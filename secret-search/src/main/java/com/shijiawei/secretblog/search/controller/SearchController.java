package com.shijiawei.secretblog.search.controller;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.search.service.ElasticSearchService;
import document.ArticlePreviewDocument;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ClassName: SearchController
 * Description: 搜索控制器
 * 提供文章搜索相關的 API 端點
 *
 * @Create 2025/12/11
 */
@Slf4j
@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private ElasticSearchService elasticSearchService;

    /**
     * 高亮搜索文章
     * 根據關鍵字搜索文章，返回包含高亮標記的搜索結果
     *
     * @param keyword    搜索關鍵字
     * @param page       頁碼（從 0 開始，預設為 0）
     * @param size       每頁數量（預設為 10）
     * @param categoryId 文章分類 ID（可選）
     * @return 包含高亮內容的分頁搜索結果
     */
    @GetMapping("/highlight")
    public R<Page<ArticlePreviewDocument>> searchWithHighlight(
            @RequestParam(value = "keyword") String keyword,
//            @RequestParam(value = "searchType" , required = false) String searchType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId
    ) {

        log.info("執行高亮搜索，keyword={}，page={}，size={}，categoryId={}", keyword, page, size, categoryId);
        
        // 建立分頁參數
        Pageable pageable = PageRequest.of(page, size);
        
        // 執行高亮搜索，預設搜索 title 和 content 欄位
//        Page<ArticlePreviewDocument> result = elasticSearchService.searchWithHighlight(
//                searchType,keyword, pageable, fields);

        Page<ArticlePreviewDocument> result = elasticSearchService.searchWithHighlight(
                keyword, pageable, categoryId);
        log.info("高亮搜索完成，共找到 {} 筆結果", result.getTotalElements());
        
        return R.ok(result);
    }

//    /**
//     * 自訂欄位高亮搜索文章
//     * 可指定搜索欄位，返回包含高亮標記的搜索結果
//     *
//     * @param keyword 搜索關鍵字
//     * @param fields  要搜索的欄位（例如：title, content, nickName）
//     * @param page    頁碼（從 0 開始，預設為 0）
//     * @param size    每頁數量（預設為 10）
//     * @return 包含高亮內容的分頁搜索結果
//     */
//    @GetMapping("/highlight/custom")
//    public R<Page<ArticlePreviewDocument>> searchWithHighlightCustomFields(
//            @RequestParam(value = "keyword") String keyword,
//            @RequestParam(value = "fields") String[] fields,
//            @RequestParam(value = "page", defaultValue = "0") int page,
//            @RequestParam(value = "size", defaultValue = "10") int size) {
//
//        log.info("執行自訂欄位高亮搜索，keyword={}，fields={}，page={}，size={}",
//                keyword, String.join(",", fields), page, size);
//
//        // 建立分頁參數
//        Pageable pageable = PageRequest.of(page, size);
//
//        // 執行高亮搜索
//        Page<ArticlePreviewDocument> result = elasticSearchService.searchWithHighlight(
//                keyword, pageable, fields);
//
//        log.info("自訂欄位高亮搜索完成，共找到 {} 筆結果", result.getTotalElements());
//
//        return R.ok(result);
//    }

    /**
     * 建立單篇文章的 Elasticsearch 預覽文檔
     * 根據 articleId 從資料庫獲取文章資料並建立 ES 文檔
     *
     * @param articleId 文章 ID
     * @return 操作結果
     */
    @PostMapping("/index/article/{articleId}")
    public R<Void> createArticlePreviewDoc(@PathVariable Long articleId) {
        log.info("開始建立單篇文章 ES 文檔，articleId={}", articleId);
        
        try {
            elasticSearchService.createArticlePreviewDoc(articleId);
            log.info("成功建立文章 ES 文檔，articleId={}", articleId);
            return R.ok();
        } catch (Exception e) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.ARTICLE_ES_INDEX_ERROR)
                    .detailMessage("建立文章 ES 文檔失敗: " + e.getMessage())
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, 0L)))
                    .build();
        }
    }

    /**
     * 批量建立文章的 Elasticsearch 預覽文檔
     * 根據 articleId 列表從資料庫獲取文章資料並批量建立 ES 文檔
     *
     * @param articleIds 文章 ID 列表
     * @return 操作結果
     */
    @PostMapping("/index/articles/batch")
    public R<Void> createArticlePreviewDocByList(@RequestBody List<Long> articleIds) {
        log.info("開始批量建立文章 ES 文檔，共 {} 篇", articleIds != null ? articleIds.size() : 0);
        
        try {
            elasticSearchService.createArticlePreviewDocByList(articleIds);
            log.info("成功批量建立文章 ES 文檔，共 {} 篇", articleIds != null ? articleIds.size() : 0);
            return R.ok();
        } catch (Exception e) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.ARTICLE_ES_INDEX_ERROR)
                    .detailMessage("批量建立文章 ES 文檔失敗: " + e.getMessage())
                    .data(ObjectUtils.defaultIfNull(articleIds, 0L))
                    .build();
        }
    }
}
