package com.shijiawei.secretblog.article.config;


import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: RabbitConfig
 * Description:
 *
 * @Create 2025/12/1 下午6:07
 */
@Configuration
public class RabbitConfig {

    @Bean
    public Queue commentActionQueue() {
        return new Queue("Article Notification Queue");
    }
}
