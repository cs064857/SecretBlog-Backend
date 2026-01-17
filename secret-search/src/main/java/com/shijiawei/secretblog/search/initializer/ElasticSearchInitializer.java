package com.shijiawei.secretblog.search.initializer;

import com.shijiawei.secretblog.search.service.ElasticSearchService;
import document.ArticlePreviewDocument;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * ClassName: ElasticSearchInitializer
 * Description:
 *
 * @Create 2025/12/10 下午5:17
 */
@Slf4j
@Component
public class ElasticSearchInitializer {

    @Autowired
    private ElasticSearchService elasticSearchService;


    /**
     * 運行服務時, 當ArticlePreviewDocument的索引不存在或不完整時則進行初始化
     */
    @Retryable(
            retryFor = {Exception.class, RuntimeException.class},
            maxAttempts = 5, //最多重試5次
            backoff = @Backoff(delay = 10000) //間隔10秒
    )
    @EventListener(ApplicationReadyEvent.class)
    public void initElasticSearchArticlePreviewDocument() {
        log.info("初始化 Elasticsearch 文章預覽索引...");

        //先檢查索引是否已經存在
        boolean ensureIndexExists = elasticSearchService.ensureIndexExists(ArticlePreviewDocument.class);

        if (ensureIndexExists) {
            log.info("索引存在, 檢查索引完整性...");

            //索引存在，進一步檢查索引完整性(ES文檔數 vs 資料庫文章數進行比對)
            boolean isIndexComplete = elasticSearchService.isArticlePreviewIndexComplete();
            log.info("索引完整性檢查結果: {}", isIndexComplete);
            if (isIndexComplete) {
                long indexDocCount = elasticSearchService.getArticlePreviewIndexDocCount();
                log.info("跳過初始化, 索引已完整建立, ES文檔數量:{}", indexDocCount);
                return;
            } else {
                log.info("索引存在但不完整，將重新建立所有文檔...");
            }
        }

        log.info("開始初始化 Elasticsearch 文章預覽索引...");
        elasticSearchService.createArticlePreviewAllListDoc();
        log.info("Elasticsearch 文章預覽索引初始化完成");
    }

    /**
     * 重試耗盡後的恢復方法
     */
    @Recover
    public void recoverFromInitFailure(Exception e) {
        log.error("Elasticsearch 初始化重試耗盡(已重試5次)，將在後續手動觸發: {}", e.getMessage());
    }

}
