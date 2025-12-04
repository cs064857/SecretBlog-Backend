package com.shijiawei.secretblog.user.rabbit.producer;

import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: UmsLocalMessageProducer
 * Description:
 *
 * @Create 2025/12/1 下午6:28
 */
@Slf4j
@Component
public class UmsLocalMessageProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendNotification(RabbitMessage message){
        rabbitTemplate.convertAndSend(message.getExchange(),message.getRoutingKey(), message);
        log.info("將RabbitMQ訊息發送至佇列： {}", message);

    }

//    public void sendNotification(RabbitMessage message){
//        rabbitTemplate.convertAndSend(message.getExchange(),message.getRoutingKey(), message);
//        log.info("Sent message to queue: {}", message);
//
//    }

}
