package com.shijiawei.secretblog.common.message;

import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 通用本地消息 RabbitMQ 生產者
 * 所有需要發送 RabbitMQ 消息的模組皆可直接使用此類
 * 合併自 AmsProducer 與 UmsLocalMessageProducer
 *
 * @Create 2025/12/5
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMessageProducer {

    private final RabbitTemplate rabbitTemplate;

    /**
     * 發送消息到 RabbitMQ
     *
     * @param message 實作 RabbitMessage 介面的消息物件
     */
    public void send(RabbitMessage message) {
        rabbitTemplate.convertAndSend(message.getExchange(), message.getRoutingKey(), message);
        log.info("已發送 RabbitMQ 訊息：exchange={}, routingKey={}",
                message.getExchange(), message.getRoutingKey());
    }

    /**
     * 在交易提交後才發送 RabbitMQ 訊息。
     *
     * 用途：避免 DB 尚未 commit 就先送出通知，造成消費端查詢不到最新資料或出現不一致。
     *
     * @param message 實作 RabbitMessage 介面的消息物件
     */
    public void sendAfterCommit(RabbitMessage message) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            log.warn("當前不在事務中，直接發送 RabbitMQ 訊息：exchange={}, routingKey={}",
                    message.getExchange(), message.getRoutingKey());
            send(message);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    send(message);
                } catch (Exception e) {
                    log.error("事務提交後發送 RabbitMQ 訊息失敗：exchange={}, routingKey={}",
                            message.getExchange(), message.getRoutingKey(), e);
                }
            }
        });
    }

}
