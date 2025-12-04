package com.shijiawei.secretblog.user.rabbit.producer;

import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: AuthorInfoUpdateProducer
 * Description:
 *
 * @Create 2025/12/1 下午6:28
 */
@Slf4j
@Component
public class AuthorInfoUpdateProducer {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendAuthorInfoUpdateNotification(AuthorInfoUpdateMessage authorInfoUpdateMessage){
        rabbitTemplate.convertAndSend(authorInfoUpdateMessage.getExchange(),authorInfoUpdateMessage.getRoutingKey(), authorInfoUpdateMessage);
        log.info("Sent author info update message to queue: {}", authorInfoUpdateMessage);

    }

//    public void sendNotification(RabbitMessage message){
//        rabbitTemplate.convertAndSend(message.getExchange(),message.getRoutingKey(), message);
//        log.info("Sent message to queue: {}", message);
//
//    }

}
