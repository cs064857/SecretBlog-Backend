package com.shijiawei.secretblog.common.message;

import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

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

}
