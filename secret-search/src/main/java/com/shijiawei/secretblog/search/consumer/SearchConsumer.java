package com.shijiawei.secretblog.search.consumer;

import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import com.shijiawei.secretblog.common.message.SyncArticleToESMessage;
import com.shijiawei.secretblog.search.service.ElasticSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: SearchConsumer
 * Description: 搜索服務消息消費者
 * 負責監聽 RabbitMQ 消息並執行 Elasticsearch 同步操作
 *
 * @Create 2025/12/11
 */
@Slf4j
@Component
public class SearchConsumer {

    @Autowired
    private ElasticSearchService elasticSearchService;

    /**
     * 處理文章同步至 Elasticsearch 的消息
     * @param message 同步消息
     */
    @RabbitListener(queues = RabbitMqConsts.search.syncArticleToES.queue)
    public void handleSyncArticleToES(SyncArticleToESMessage message) {
        log.info("RabbitMQ 收到文章同步至 ES 消息，articleId={}，operationType={}",
                message.getArticleId(), message.getOperationType());
        
        try {
            String operationType = message.getOperationType();
            
            if (SyncArticleToESMessage.OPERATION_DELETE.equals(operationType)) {
                // 刪除操作
                elasticSearchService.deleteArticlePreviewDoc(message.getArticleId());
            } else {
                // CREATE 和 UPDATE 操作都使用同一方法（save 會自動處理新增或更新）
                elasticSearchService.createArticlePreviewDoc(message.getArticleId());
            }
            
            log.info("RabbitMQ 文章同步至 ES 成功，articleId={}，operationType={}",
                    message.getArticleId(), message.getOperationType());
        } catch (Exception e) {
            log.error("RabbitMQ 文章同步至 ES 失敗，articleId={}，operationType={}，error={}",
                    message.getArticleId(), message.getOperationType(), e.getMessage(), e);
            
            // 拋出運行時異常，讓消息隊列進行重試
            throw new RuntimeException("文章同步至 ES 失敗", e);
        }
    }
}
