package com.shijiawei.secretblog.common.message;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.common.codeEnum.LocalMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息調度器抽象基類
 * 子類別只需實現 getLocalMessageService() 和 processMessage() 方法
 *
 * @param <T> 消息實體類型（如 UmsLocalMessage, AmsLocalMessage）
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

    /**
     * 獲取本地消息服務（由子類實現）
     * @return 本地消息服務實例
     */
    protected abstract LocalMessageService<T> getLocalMessageService();

    /**
     * 處理單條消息（由子類實現）
     * 子類別負責反序列化消息內容並發送到 RabbitMQ
     *
     * @param message 本地消息
     * @throws Exception 處理失敗時拋出異常
     */
    protected abstract void processMessage(T message) throws Exception;

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

        log.info("Found {} pending local messages to dispatch", messages.size());

        for (T message : messages) {
            try {
                processMessage(message);
                getLocalMessageService().markAsSent(message.getId());
            } catch (Exception e) {
                log.error("Failed to dispatch local message, id={}", message.getId(), e);
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
