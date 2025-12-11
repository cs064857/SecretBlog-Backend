package com.shijiawei.secretblog.search;

import com.shijiawei.secretblog.common.dto.AmsArtTagsDTO;
import com.shijiawei.secretblog.common.dto.ArticlePreviewDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.search.feign.ArticleFeignClient;
import com.shijiawei.secretblog.search.service.ElasticSearchService;
import com.shijiawei.secretblog.search.vo.AmsArtTagsVo;
import document.ArticlePreviewDocument;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.data.domain.Pageable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
@Slf4j
@SpringBootTest
@ActiveProfiles("local")

class SecretSearchApplicationTests {

    static {
        System.setProperty("nacos.server.grpc.port.offset", "1");
    }

    @Autowired
    private ArticleFeignClient articleFeignClient;


    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Test
    void testGetArticlePreviewForSearch() {
        // 使用已存在的文章 ID
        Long articleId = 1951352728963932200L; // 替換為實際存在的文章ID

        R<ArticlePreviewDTO> response = articleFeignClient.getArticlePreviewForSearch(articleId);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(articleId, response.getData().getArticleId());
        System.out.println("成功獲取文章預覽: " + response.getData().getTitle());
    }

    @Test
    void testGetArticlePreviewAndCreateDOC(){
        Long articleId = 1951352728963932200L; // 替換為實際存在的文章ID

        // 確保索引存在並刷新
        operations.indexOps(ArticlePreviewDocument.class).refresh();

        // 透過 Feign 調用 secret-article 獲取文章預覽資料
        R<ArticlePreviewDTO> response = articleFeignClient.getArticlePreviewForSearch(articleId);
        if (response == null || response.getData() == null) {
            log.error("無法獲取文章預覽資料，articleId={}", articleId);
            throw new RuntimeException("無法獲取文章預覽資料: articleId=" + articleId);
        }
        ArticlePreviewDTO preview = response.getData();
        log.debug("成功獲取文章預覽資料，articleId={}，title={}", articleId, preview.getTitle());

        //去除HTML格式只保留純文字
        String cleanText = Jsoup.parse(preview.getContent()).text(); // 剝除 HTML 標籤取純文字

        // 轉換標籤 DTO 為內部使用的標籤對象
        List<AmsArtTagsVo> tagList = convertTagsToVo(preview.getAmsArtTagList());

        // 使用 Builder 構建 ArticlePreviewDocument 對象
        ArticlePreviewDocument articleDocument = ArticlePreviewDocument.builder()
                .id("article_" + preview.getArticleId())
                .articleId(preview.getArticleId())
                .userId(preview.getUserId())
                .nickName(preview.getNickName())
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
        log.info("文章預覽 ES 文檔建立完成，articleId={}，esDocId={}", articleId, articleDocument.getId());
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

    @Test
    void highlightSearchArticlePreview(){
        String keyword = "最幸福";
        Pageable pageable = PageRequest.of(0,10);
        Page<ArticlePreviewDocument> result = elasticSearchService.searchWithHighlight(keyword, pageable, "title");
        List<ArticlePreviewDocument> content = result.getContent();
        content.forEach(item->{
            System.out.println("搜尋結果:"+item);

        });
    }
}
