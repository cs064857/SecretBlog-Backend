package com.shijiawei.secretblog.user.rabbit.producer;

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
        rabbitTemplate.convertAndSend("commentActionDirectExchange","", authorInfoUpdateMessage);
        log.info("Sent author info update message to queue: {}", authorInfoUpdateMessage);

    }


}
