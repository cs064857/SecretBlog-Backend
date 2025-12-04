package com.shijiawei.secretblog.article.config;


import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
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
    public MessageConverter messageConverter() {
        // 使用 JSON 轉換器替代預設的 SimpleMessageConverter
        return new Jackson2JsonMessageConverter();
    }
}
