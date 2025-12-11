package com.shijiawei.secretblog.search.repository;

import document.ArticlePreviewDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/**
 * ClassName: ArticlePreviewDocumentRepository
 * Description:
 *
 * @Create 2025/12/11 上午12:11
 */
@Component
public interface ArticlePreviewDocumentRepository
        extends ElasticsearchRepository<ArticlePreviewDocument, String> {
}