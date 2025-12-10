package com.shijiawei.secretblog.search.feign;

import com.shijiawei.secretblog.common.dto.ArticlePreviewDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.search.config.FeignInterceptorConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 文章微服務遠程調用客戶端
 * 用於 secret-search 調用 secret-article 獲取文章預覽資料
 */
@FeignClient(name = "secret-article", path = "/article", configuration = FeignInterceptorConfig.class)
public interface ArticleFeignClient {

    /**
     * 獲取文章預覽資料
     * @param articleId 文章ID
     * @return 文章預覽 DTO
     */
    @GetMapping("/internal/preview/{articleId}")
    R<ArticlePreviewDTO> getArticlePreviewForSearch(@PathVariable("articleId") Long articleId);

    /**
     * 批量獲取文章預覽資料
     * @param articleIds 文章 ID 列表
     * @return 文章預覽 DTO 列表
     */
    @PostMapping("/internal/preview/batch")
    R<List<ArticlePreviewDTO>> getBatchArticlePreviewsForSearch(@RequestBody List<Long> articleIds);

    /**
     * 獲取所有文章 ID
     * @return 所有文章 ID 列表
     */
    @GetMapping("/internal/article-ids")
    R<List<Long>> getAllDistinctArticleIds();

    /**
     * 獲取文章總數量
     * @return 文章總數量
     */
    @GetMapping("/internal/article-count")
    R<Long> getTotalArticleCount();
}
