package com.shijiawei.secretblog.common.message;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.common.codeEnum.LocalMessage;
import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息服務通用介面
 * @param <T> 具體的本地消息實體類型，必須實作 LocalMessage 介面
 */
public interface LocalMessageService<T extends LocalMessage> extends IService<T> {

    /**
     * 建立待發送的本地消息
     * @param message RabbitMQ 消息
     * @return 已建立的本地消息
     */
    T createPendingMessage(RabbitMessage message);

    /**
     * 查詢待發送的本地消息
     * @param now 當前時間
     * @param limit 最大筆數
     * @return 待發送消息列表
     */
    List<T> fetchMessagesToSend(LocalDateTime now, int limit);

    /**
     * 標記消息為已發送
     * @param id 消息ID
     */
    void markAsSent(Long id);

    /**
     * 標記消息為失敗並安排重試
     * @param id 消息ID
     * @param errorMsg 錯誤訊息
     * @param maxRetryCount 最大重試次數
     * @param initialDelaySeconds 初始重試延遲秒數
     * @param maxDelaySeconds 最大重試延遲秒數
     */
    void markAsFailedAndScheduleRetry(Long id, String errorMsg,
            int maxRetryCount, int initialDelaySeconds, int maxDelaySeconds);
}
