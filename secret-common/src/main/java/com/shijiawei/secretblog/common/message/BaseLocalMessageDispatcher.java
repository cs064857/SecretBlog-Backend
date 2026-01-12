package com.shijiawei.secretblog.common.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.common.codeEnum.LocalMessage;
import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息調度器抽象基類
 * 子類別只需實現 getLocalMessageService() 方法
 * 
 * 預設提供 dispatch() 方法進行定時調度，子類可覆寫以自訂調度策略
 *
*/
@Slf4j
public abstract class BaseLocalMessageDispatcher<T extends LocalMessage> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${local-message.dispatcher.max-retry-count:5}")
    protected int maxRetryCount;

    @Value("${local-message.dispatcher.initial-delay-seconds:10}")
    protected int initialDelaySeconds;

    @Value("${local-message.dispatcher.max-delay-seconds:3600}")
    protected int maxDelaySeconds;

    @Value("${local-message.dispatcher.batch-size:100}")
    protected int batchSize;

    @Autowired
    private RabbitMessageProducer rabbitMessageProducer;

    /**
     * 獲取本地消息服務（由子類實現)
     * @return 本地消息服務實例
     */
    protected abstract LocalMessageService<T> getLocalMessageService();

    /**
     * 定時調度待發送的消息（默認實現)
     * 子類可覆寫此方法以自訂調度策略（如不同的調度頻率或條件判斷)
     */
    @Scheduled(fixedDelayString = "${local-message.dispatcher.fixed-delay-ms:5000}")
    public void dispatch() {
        dispatchPendingMessages();
    }

    protected void processMessage(T message) throws Exception {
        // 反序列化消息內容（使用 Jackson 多態類型自動識別具體類型)
        RabbitMessage payload = objectMapper.readValue(
                message.getContent(),
                RabbitMessage.class
        );
        // 發送消息到 RabbitMQ
        rabbitMessageProducer.send(payload);
    }


    /**
     * 調度待發送的消息
     * 此方法可被 @Scheduled 標註的子類別方法調用
     */
    public void dispatchPendingMessages() {
        LocalDateTime now = LocalDateTime.now();
        List<T> messages = getLocalMessageService().fetchMessagesToSend(now, batchSize);

        if (messages.isEmpty()) {
            return;
        }

        log.info("找到 {} 筆待發送的訊息", messages.size());

        for (T message : messages) {
            try {
                processMessage(message);
                getLocalMessageService().markAsSent(message.getId());
            } catch (Exception e) {
                log.error("無法分派本機訊息，ID 為 {}", message.getId(), e);
                getLocalMessageService().markAsFailedAndScheduleRetry(
                        message.getId(),
                        e.getMessage(),
                        maxRetryCount,
                        initialDelaySeconds,
                        maxDelaySeconds
                );
            }
        }
    }

    
}
