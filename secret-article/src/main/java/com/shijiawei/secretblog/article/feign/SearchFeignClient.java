package com.shijiawei.secretblog.article.feign;

import com.shijiawei.secretblog.article.config.FeignInterceptorConfig;
import com.shijiawei.secretblog.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * ClassName: SearchFeignClient
 * Description: 搜索微服務遠程調用客戶端
 * 用於調用 secret-search 服務的 Elasticsearch 相關功能
 *
 * @Create 2025/12/11
 */
@FeignClient(name = "secret-search", path = "/search", configuration = FeignInterceptorConfig.class)
public interface SearchFeignClient {

    /**
     * 建立單篇文章的 Elasticsearch 預覽文檔
     * @param articleId 文章 ID
     * @return 操作結果
     */
    @PostMapping("/index/article/{articleId}")
    R<Void> createArticlePreviewDoc(@PathVariable("articleId") Long articleId);

    /**
     * 批量建立文章的 Elasticsearch 預覽文檔
     * @param articleIds 文章 ID 列表
     * @return 操作結果
     */
    @PostMapping("/index/articles/batch")
    R<Void> createArticlePreviewDocByList(@RequestBody List<Long> articleIds);
}
