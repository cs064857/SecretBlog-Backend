package com.shijiawei.secretblog.common.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.common.codeEnum.LocalMessage;
import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 本地消息服務的抽象父類別
 * @param <M> 具體的 Mapper (如 UmsLocalMessageMapper)
 * @param <T> 具體的實體 (如 UmsLocalMessage)，必須實作 LocalMessage 介面
 */
@Slf4j
public abstract class BaseLocalMessageService<M extends BaseMapper<T>, T extends LocalMessage>
        extends ServiceImpl<M, T> {

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final int DEFAULT_MAX_RETRY_COUNT = 5;
    protected static final int DEFAULT_INITIAL_DELAY_SECONDS = 10;
    protected static final int DEFAULT_MAX_DELAY_SECONDS = 3600;

    /**
     * 通用的建立待發送消息方法
     * @param localMessage 本地消息實體
     * @param message RabbitMQ 消息
     * @return 已設置好的本地消息實體
     */
    public T createPendingMessage(T localMessage, RabbitMessage message) {
        localMessage.setExchange(message.getExchange());
        localMessage.setRoutingKey(message.getRoutingKey());
        localMessage.setMsgId(UUID.randomUUID().toString());

        try {
            localMessage.setContent(objectMapper.writeValueAsString(message));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize message content", e);
        }

        // 設置狀態與時間
        localMessage.setStatus(0); // 待發送
        localMessage.setRetryCount(0);

        LocalDateTime now = LocalDateTime.now();
        localMessage.setNextRetryAt(now);
        localMessage.setCreateAt(now);
        localMessage.setUpdateAt(now);

        return localMessage;
    }

    /**
     * 標記消息為已發送
     * @param id 消息ID
     */
    public void markAsSent(Long id) {
        T message = this.getById(id);
        if (message == null) {
            log.warn("Local message not found for id={} when marking as sent", id);
            return;
        }
        message.setStatus(1);
        message.setUpdateAt(LocalDateTime.now());
        message.setErrorMsg(null);
        this.updateById(message);
    }

    /**
     * 標記消息為失敗並安排重試（指數退避策略）
     * @param id 消息ID
     * @param errorMsg 錯誤訊息
     * @param maxRetryCount 最大重試次數
     * @param initialDelaySeconds 初始延遲秒數
     * @param maxDelaySeconds 最大延遲秒數
     */
    public void markAsFailedAndScheduleRetry(Long id, String errorMsg,
            int maxRetryCount, int initialDelaySeconds, int maxDelaySeconds) {
        T message = this.getById(id);
        if (message == null) {
            log.warn("Local message not found for id={} when scheduling retry", id);
            return;
        }

        int currentRetry = message.getRetryCount() == null ? 0 : message.getRetryCount();
        currentRetry++;
        message.setRetryCount(currentRetry);
        message.setUpdateAt(LocalDateTime.now());
        message.setErrorMsg(errorMsg);

        if (currentRetry >= maxRetryCount) {
            message.setStatus(2); // 發送失敗
            log.error("Local message id={} reached max retry count {}, marked as failed", id, maxRetryCount);
        } else {
            message.setStatus(0); // 待重試
            long delay = (long) (initialDelaySeconds * Math.pow(2, currentRetry - 1));
            if (delay > maxDelaySeconds) {
                delay = maxDelaySeconds;
            }
            LocalDateTime nextRetryAt = LocalDateTime.now().plusSeconds(delay);
            message.setNextRetryAt(nextRetryAt);
        }
        this.updateById(message);
    }
}
