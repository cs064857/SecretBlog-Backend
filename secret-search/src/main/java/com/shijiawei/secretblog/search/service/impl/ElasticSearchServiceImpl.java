package com.shijiawei.secretblog.search.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.MoreLikeThisQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryVariant;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.dto.AmsArtTagsDTO;
import com.shijiawei.secretblog.common.dto.ArticlePreviewDTO;
import com.shijiawei.secretblog.common.exception.BusinessException;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.search.feign.ArticleFeignClient;
import com.shijiawei.secretblog.search.repository.ArticlePreviewDocumentRepository;
import com.shijiawei.secretblog.search.service.ElasticSearchService;
import com.shijiawei.secretblog.search.vo.AmsArtTagsVo;
import document.ArticlePreviewDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;
import org.jsoup.Jsoup;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Elasticsearch 服務實現類
 * 負責建構和管理 Elasticsearch 文檔索引
 */
@Slf4j
@Service
public class ElasticSearchServiceImpl implements ElasticSearchService {

    @Autowired
    private ArticleFeignClient articleFeignClient;

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private ArticlePreviewDocumentRepository articlePreviewDocumentRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    /**
     * 根據 articleId 建立文章預覽 Elasticsearch 文檔
     *
     * @param articleId 文章ID
     */
    public void createArticlePreviewDoc(Long articleId) {
        log.info("開始建立文章預覽 ES 文檔，articleId={}", articleId);

        // 確保索引存在（若不存在則創建並設定映射）
//        ensureIndexExists();

        // 透過 Feign 調用 secret-article 獲取文章預覽資料
        R<ArticlePreviewDTO> response = articleFeignClient.getArticlePreviewForSearch(articleId);
        if (response == null || response.getData() == null) {
            log.error("無法獲取文章預覽資料，articleId={}", articleId);
            throw new RuntimeException("無法獲取文章預覽資料: articleId=" + articleId);
        }
        ArticlePreviewDTO preview = response.getData();
        log.debug("成功獲取文章預覽資料，articleId={}，title={}", articleId, preview.getTitle());

        // 轉換標籤 DTO 為內部使用的標籤對象
        List<AmsArtTagsVo> tagList = convertTagsToVo(preview.getAmsArtTagList());

        //去除HTML格式只保留純文字
        String cleanText = Jsoup.parse(preview.getContent()).text(); // 剝除 HTML 標籤取純文字


        // 使用 Builder 構建 ArticlePreviewDocument 對象
        ArticlePreviewDocument articleDocument = ArticlePreviewDocument.builder()
                .id("article_" + preview.getArticleId())
                .articleId(preview.getArticleId())
                .userId(preview.getUserId())
                .nickName(preview.getNickName())
                .avatar(preview.getAvatar())
                .title(preview.getTitle())
                .content(cleanText)
                .categoryId(preview.getCategoryId())
                .categoryName(preview.getCategoryName())
                .amsArtTagList(tagList)
                .createTime(preview.getCreateTime())
                .updateTime(preview.getUpdateTime())
                .build();

        // 儲存文檔到 Elasticsearch
        operations.save(articleDocument);

        // 刷新索引，使變更立即可見
        operations.indexOps(ArticlePreviewDocument.class).refresh();

        log.info("文章預覽 ES 文檔建立完成，articleId={}，esDocId={}", articleId, articleDocument.getId());
    }

    /**
     * 批量建立所有文章預覽 Elasticsearch 文檔
     * 用於 ElasticSearch 初始化時一次性建立所有 ArticlePreview 的 Doc
     */
    public void createArticlePreviewAllListDoc() {
        log.info("開始批量建立所有文章預覽 ES 文檔");

        // 確保索引存在（若不存在則創建並設定映射）
//        ensureIndexExists();

        // 透過 Feign 調用 secret-article 獲取所有文章 ID
        R<List<Long>> articleIdsResponse = articleFeignClient.getAllDistinctArticleIds();
        if (articleIdsResponse == null || articleIdsResponse.getData() == null || articleIdsResponse.getData().isEmpty()) {
            log.warn("無法獲取任何文章 ID，跳過批量 ES 文檔建立");
            return;
        }
        List<Long> articleIds = articleIdsResponse.getData();
        log.info("成功獲取文章 ID 列表，共 {} 篇文章", articleIds.size());

        // 透過批量 Feign 獲取所有文章預覽資料
        R<List<ArticlePreviewDTO>> batchResponse = articleFeignClient.getBatchArticlePreviewsForSearch(articleIds);
        if (batchResponse == null || batchResponse.getData() == null || batchResponse.getData().isEmpty()) {
            log.warn("批量獲取文章預覽資料為空，跳過 ES 文檔建立");
            return;
        }
        List<ArticlePreviewDTO> previews = batchResponse.getData();
        log.info("成功批量獲取文章預覽資料，共 {} 篇", previews.size());

        // 批量建立 Elasticsearch 文檔
        List<ArticlePreviewDocument> documents = new ArrayList<>();
        for (ArticlePreviewDTO preview : previews) {
            try {
                // 轉換標籤 DTO 為內部使用的標籤對象
                List<AmsArtTagsVo> tagList = convertTagsToVo(preview.getAmsArtTagList());

                // 去除 HTML 格式只保留純文字
                String cleanText = Jsoup.parse(preview.getContent()).text();

                // 使用 Builder 構建 ArticlePreviewDocument 對象
                ArticlePreviewDocument articleDocument = ArticlePreviewDocument.builder()
                        .id("article_" + preview.getArticleId())
                        .articleId(preview.getArticleId())
                        .userId(preview.getUserId())
                        .nickName(preview.getNickName())
                        .avatar(preview.getAvatar())
                        .title(preview.getTitle())
                        .content(cleanText)
                        .categoryId(preview.getCategoryId())
                        .categoryName(preview.getCategoryName())
                        .amsArtTagList(tagList)
                        .createTime(preview.getCreateTime())
                        .updateTime(preview.getUpdateTime())
                        .build();

                documents.add(articleDocument);
            } catch (Exception e) {
                log.warn("建立文章預覽 ES 文檔時發生錯誤，跳過 articleId={}，error={}", preview.getArticleId(), e.getMessage());
            }
        }

        // 批量儲存文檔到 Elasticsearch
        if (!documents.isEmpty()) {
            operations.save(documents);
            // 刷新索引，使變更立即可見
            operations.indexOps(ArticlePreviewDocument.class).refresh();
            log.info("批量建立文章預覽 ES 文檔完成，成功建立 {} 筆文檔", documents.size());
        } else {
            log.warn("無任何文章預覽 ES 文檔可建立");
        }
    }

    /**
     * 根據指定的 articleId 列表批量建立文章預覽 Elasticsearch 文檔
     *
     * @param articleIds 需要建立 ES 文檔的文章 ID 列表
     */
    public void createArticlePreviewDocByList(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            log.warn("傳入的 articleIds 為空，跳過批量 ES 文檔建立");
            return;
        }
        log.info("開始根據指定列表批量建立文章預覽 ES 文檔，共 {} 篇文章", articleIds.size());

        // 確保索引存在（若不存在則創建並設定映射）
//        ensureIndexExists();

        // 透過批量 Feign 獲取指定文章預覽資料
        R<List<ArticlePreviewDTO>> batchResponse = articleFeignClient.getBatchArticlePreviewsForSearch(articleIds);
        if (batchResponse == null || batchResponse.getData() == null || batchResponse.getData().isEmpty()) {
            log.warn("批量獲取文章預覽資料為空，跳過 ES 文檔建立");
            return;
        }
        List<ArticlePreviewDTO> previews = batchResponse.getData();
        log.info("成功批量獲取文章預覽資料，共 {} 篇", previews.size());

        // 批量建立 Elasticsearch 文檔
        List<ArticlePreviewDocument> documents = new ArrayList<>();
        for (ArticlePreviewDTO preview : previews) {
            try {
                // 轉換標籤 DTO 為內部使用的標籤對象
                List<AmsArtTagsVo> tagList = convertTagsToVo(preview.getAmsArtTagList());

                // 去除 HTML 格式只保留純文字
                String cleanText = Jsoup.parse(preview.getContent()).text();

                // 使用 Builder 構建 ArticlePreviewDocument 對象
                ArticlePreviewDocument articleDocument = ArticlePreviewDocument.builder()
                        .id("article_" + preview.getArticleId())
                        .articleId(preview.getArticleId())
                        .userId(preview.getUserId())
                        .nickName(preview.getNickName())
                        .avatar(preview.getAvatar())
                        .title(preview.getTitle())
                        .content(cleanText)
                        .categoryId(preview.getCategoryId())
                        .categoryName(preview.getCategoryName())
                        .amsArtTagList(tagList)
                        .createTime(preview.getCreateTime())
                        .updateTime(preview.getUpdateTime())
                        .build();

                documents.add(articleDocument);
            } catch (Exception e) {
                log.warn("建立文章預覽 ES 文檔時發生錯誤，跳過 articleId={}，error={}", preview.getArticleId(), e.getMessage());
            }
        }

        // 批量儲存文檔到 Elasticsearch
        if (!documents.isEmpty()) {
            operations.save(documents);
            // 刷新索引，使變更立即可見
            operations.indexOps(ArticlePreviewDocument.class).refresh();
            log.info("根據指定列表批量建立文章預覽 ES 文檔完成，成功建立 {} 筆文檔", documents.size());
        } else {
            log.warn("無任何文章預覽 ES 文檔可建立");
        }
    }

    /**
     * 確保指定的 Elasticsearch 索引存在
     *
     * @param documentClass 文檔類型的 Class 對象
     * @param <T>           文檔類型泛型
     */
    public <T> boolean ensureIndexExists(Class<T> documentClass) {
        IndexOperations indexOps = operations.indexOps(documentClass);
        if (!indexOps.exists()) {
            String indexName = documentClass.getSimpleName();
            log.info("索引 {} 不存在", indexName);
            return false;
        }
        return true;
    }

    /**
     * 獲取索引的文檔總數量
     */
//    public long getIndexDocCount(){
//        // 使用 ElasticsearchOperations 的 count 方法計算文檔數量
//        NativeQuery countQuery = NativeQuery.builder()
//                .withQuery(q -> q.matchAll(m -> m))
//                .build();
//        return operations.count(countQuery, ArticlePreviewDocument.class);
//    }

    /**
     * 獲取索引的文檔總數量
     */
    public long getArticlePreviewIndexDocCount() {
        //直接使用repository的count取代operations的操作
        return articlePreviewDocumentRepository.count();
    }

    /**
     * 檢查 ArticlePreview 索引是否已完整建立
     * 透過比較資料庫文章數量與 ES 索引文檔數量來判斷
     *
     * @return true 表示索引完整（ES 文檔數 >= 資料庫文章數），false 表示索引不完整
     */
    @Override
    public boolean isArticlePreviewIndexComplete() {
        log.info("開始檢查 ArticlePreview 索引完整性");

        try {
            // 獲取 ES 索引的文檔數量
            long esDocCount = getArticlePreviewIndexDocCount();
            log.debug("ES 索引文檔數量: {}", esDocCount);

            // 透過 Feign 獲取資料庫中的文章總數量
            R<Long> response = articleFeignClient.getTotalArticleCount();
            if (response == null || response.getData() == null) {
                log.warn("無法獲取資料庫文章總數量，預設索引不完整");
                return false;
            }
            long dbArticleCount = response.getData();
            log.debug("資料庫文章總數量: {}", dbArticleCount);

            // 比較數量，判斷索引是否完整
            boolean isComplete = esDocCount >= dbArticleCount;
            log.info("ArticlePreview 索引完整性檢查結果: {} (ES文檔數: {}, 資料庫文章數: {})",
                    isComplete ? "完整" : "不完整", esDocCount, dbArticleCount);

            return isComplete;
        } catch (Exception e) {
            log.error("檢查索引完整性時發生錯誤: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 將 AmsArtTagsDTO 列表轉換為 AmsArtTagsVo 列表
     *
     * @param tagDTOList 標籤 DTO 列表
     * @return 標籤 VO 列表
     */
    private List<AmsArtTagsVo> convertTagsToVo(List<AmsArtTagsDTO> tagDTOList) {
        if (tagDTOList == null || tagDTOList.isEmpty()) {
            return Collections.emptyList();
        }
        return tagDTOList.stream()
                .map(dto -> {
                    AmsArtTagsVo vo = new AmsArtTagsVo();
                    vo.setId(dto.getId());
                    vo.setName(dto.getName());
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 刪除索引
     */
    public <T> boolean deleteIndex(Class<T> clzz) {
        IndexOperations indexOperations = operations.indexOps(clzz);
        return indexOperations.delete();
    }

    /**
     * 搜索
     */
    public void searchDocs() {


    }


    /**
     * 執行高亮搜索
     * @param keyword 搜索關鍵字
     * @param pageable 分頁參數
     * @return 包含高亮內容的分頁結果
     */
    @Override
    public Page<ArticlePreviewDocument> searchWithHighlight(String keyword, Pageable pageable) {
        return searchWithHighlight(keyword, pageable, null);
    }

    /**
     * 執行高亮搜索（可選依分類過濾）
     *
     * @param keyword    搜索關鍵字
     * @param pageable   分頁參數
     * @param categoryId 文章分類 ID（可選）
     * @return 包含高亮內容的分頁結果
     */
    @Override
    public Page<ArticlePreviewDocument> searchWithHighlight(String keyword, Pageable pageable, Long categoryId) {

        // 默認搜尋條件, 標題和內容
        List<String> fields = Arrays.asList("title", "content");
        // 設置高量的欄位
        List<HighlightField> highlightFields = fields.stream().map(HighlightField::new).toList();

        // 設定高亮參數：前後綴標籤 (例如黃色文字)
        HighlightParameters highlightParameters = HighlightParameters.builder()
                .withPreTags("<b style='color:yellow'>") // 關鍵字前的標籤
                .withPostTags("</b>") // 關鍵字後的標籤
                .withFragmentSize(100)// 內容過長時，每個片段的字數
                .withNumberOfFragments(1)//只取 1 個最佳片段 (避免內容過長)
                .withNoMatchSize(50)//如果內容不包含關鍵字，則返回的片段長度
                .build();

        Highlight highlight = new Highlight(highlightParameters, highlightFields);

        /*
         建構 NativeQuery
         */

        NativeQuery nativeQuery;

        if (categoryId == null) {
            // 不指定分類時，搜尋所有文章
            nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q.multiMatch(
                            m -> m.fields(fields)//設置搜索欄位
                                    .query(keyword)//搜索的關鍵字
                                    .minimumShouldMatch("75%")
                    ))
                    .withHighlightQuery(new HighlightQuery(highlight, null))//加入高亮配置
                    .withPageable(pageable)//設置分頁
                    .build();
        } else {
            // 指定分類時，搜尋指定分類的文章
            nativeQuery = NativeQuery.builder()
                    .withQuery(q -> q.bool(b -> b
                            .must(m -> m.multiMatch(mm -> mm
                                    .fields(fields)//設置搜索欄位
                                    .query(keyword)//搜索的關鍵字
                                    .minimumShouldMatch("75%")
                            ))
                            .filter(f -> f.term(t -> t
                                    .field("categoryId")//設置過濾分類的欄位
                                    .value(categoryId.toString())
                            ))
                    ))
                    .withHighlightQuery(new HighlightQuery(highlight, null))//加入高亮配置
                    .withPageable(pageable)//設置分頁
                    .build();
        }



        SearchHits<ArticlePreviewDocument> searchHits = operations.search(nativeQuery, ArticlePreviewDocument.class);

        // 處理結果將高亮片段替換回原始物件
        List<ArticlePreviewDocument> resultList = searchHits.stream().map(hit -> {
            // 獲取原始文檔
            ArticlePreviewDocument doc = hit.getContent();

            // 處理標題高亮
            List<String> titleHighlights = hit.getHighlightField("title");
            if (titleHighlights != null && !titleHighlights.isEmpty()) {
                // 如果有高亮片段，替換原始標題
                doc.setTitle(titleHighlights.get(0));
            }

            // 處理內容高亮
            List<String> contentHighlights = hit.getHighlightField("content");
            if (contentHighlights != null && !contentHighlights.isEmpty()) {
                // 如果有高亮片段，替換原始內容
                // 將內容變成只有包含關鍵字的那一小段文字，適合做列表預覽
                String string = contentHighlights.get(0);
                int maxLength = 150;
                if (string.length() > maxLength) {
                    // 手動限制內容字數，避免溢出，並在最後加上省略號
                    string = string.substring(0, maxLength) + "...";
                }

                // 設置最終的結果
                doc.setContent(string);
            }

            return doc;
        }).collect(Collectors.toList());

        // 封裝成分頁物件返回
        return new PageImpl<>(resultList, pageable, searchHits.getTotalHits());
    }


//    /**
//     * 執行高亮搜索
//     *
//     * @param keyword  搜索關鍵字
//     * @param pageable 分頁參數
//     * @return 包含高亮內容的分頁結果
//     */
//    public Page<ArticlePreviewDocument> searchWithHighlight(String searchType, String keyword, Pageable pageable, String... fields) {
//
//
//        // 代表默認搜尋為文章
//        if ("users".equals(searchType)) {
//            //代表搜尋用戶相關資訊
//            NativeQuery nativeQuery = NativeQuery.builder()
//                    .withQuery(q -> q.match(m -> m
//                            .query(keyword)
//                            .field("nickName")
//                    ))
//                    .build();
//
//            SearchHits<ArticlePreviewDocument> searchHits = operations.search(nativeQuery, ArticlePreviewDocument.class);
//
//            // 處理結果將高亮片段替換回原始物件
//            // 獲取原始文檔
//            List<ArticlePreviewDocument> resultList = searchHits.stream().map(SearchHit::getContent).toList();
//
//            return new PageImpl<>(resultList, pageable, searchHits.getTotalHits());
//        } else {
//
//
//
//
//            String[] defaultFields = {"title", "content"};
//
//
//            List<HighlightField> highlightFields = Arrays.stream(defaultFields).map(HighlightField::new).toList();
//
//            //設定高亮參數：前後綴標籤 (例如黃色文字)
//            HighlightParameters highlightParameters = HighlightParameters.builder()
//                    .withPreTags("<b style='color:yellow'>") // 關鍵字前的標籤
//                    .withPostTags("</b>") // 關鍵字後的標籤
//                    .withFragmentSize(100)// 內容過長時，每個片段的字數
//                    .withNumberOfFragments(1)//只取 1 個最佳片段 (避免內容過長)
//                    .withNoMatchSize(50)//如果內容不包含關鍵字，則返回的片段長度
//                    .build();
//
//            Highlight highlight = new Highlight(highlightParameters, highlightFields);
//
//            // 2準備搜索欄位 (合併默認與額外欄位)
//            List<String> targetFields = new ArrayList<>(List.of("title", "content"));
//            if (fields != null) {
//                Collections.addAll(targetFields, fields);
//            }
//            /*
//            建構NativeQuery
//             */
//
//            NativeQuery nativeQuery = NativeQuery.builder()
//                    .withQuery(q -> q.multiMatch(m -> m
//                            .fields(targetFields)
//                            .query(keyword)
//                            .minimumShouldMatch("75%"))
//                    )
//                    .withHighlightQuery(new HighlightQuery(highlight,null))
//                    .build();
//
//
//            SearchHits<ArticlePreviewDocument> searchHits = operations.search(nativeQuery, ArticlePreviewDocument.class);
//
//
//            // 處理結果將高亮片段替換回原始物件
//            List<ArticlePreviewDocument> resultList = searchHits.stream().map(hit -> {
//                // 獲取原始文檔
//                ArticlePreviewDocument doc = hit.getContent();
//
//                // 處理標題高亮
//                List<String> titleHighlights = hit.getHighlightField("title");
//                if (titleHighlights != null && !titleHighlights.isEmpty()) {
//                    // 如果有高亮片段，替換原始標題
//                    doc.setTitle(titleHighlights.get(0));
//                }
//
//                // 處理內容高亮
//                List<String> contentHighlights = hit.getHighlightField("content");
//                if (contentHighlights != null && !contentHighlights.isEmpty()) {
//                    // 如果有高亮片段，替換原始內容
//
//                    // 將內容變成只有包含關鍵字的那一小段文字，適合做列表預覽
//                    // 判斷內容字元是否超過maxLength
//                    String string = contentHighlights.get(0);
//                    int maxLength = 150;
//                    if (string.length() > maxLength) {
//                        //手動限制內容字數, 避免溢出 , 並在最後加上省略號
//                        string = string.substring(0, maxLength) + "...";
//                    }
//
//                    //設置最終的結果
//                    doc.setContent(string);
//                }
//
//
//                return doc;
//            }).collect(Collectors.toList());
//
//            return new PageImpl<>(resultList, pageable, searchHits.getTotalHits());
//        }
//
//    }

    /**
     * 根據 articleId 刪除文章預覽 Elasticsearch 文檔
     *
     * @param articleId 文章ID
     */
    @Override
    public void deleteArticlePreviewDoc(Long articleId) {
        log.info("開始刪除文章預覽 ES 文檔，articleId={}", articleId);

        String esDocId = "article_" + articleId;

        try {
            // 使用 repository 刪除文檔
            articlePreviewDocumentRepository.deleteById(esDocId);

            // 刷新索引，使變更立即可見
            operations.indexOps(ArticlePreviewDocument.class).refresh();

            log.info("文章預覽 ES 文檔刪除完成，articleId={}，esDocId={}", articleId, esDocId);
        } catch (Exception e) {
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.ARTICLE_ES_INDEX_ERROR)
                    .detailMessage("刪除文章預覽 ES 文檔失敗: articleId=" + articleId)
                    .cause(e)
                    .data(Map.of("articleId", articleId))
                    .build();
        }
    }


}
