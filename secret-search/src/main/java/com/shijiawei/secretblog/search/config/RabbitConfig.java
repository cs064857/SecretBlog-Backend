package com.shijiawei.secretblog.search.config;

import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: RabbitConfig
 * Description: secret-search 服務的 RabbitMQ 配置
 * 用於接收文章同步至 Elasticsearch 的消息
 *
 * @Create 2025/12/11
 */
@Configuration
public class RabbitConfig {

    /**
     * 搜索服務 Topic Exchange
     */
    @Bean(value = RabbitMqConsts.search.topicExchange)
    public TopicExchange searchTopicExchange() {
        return new TopicExchange(RabbitMqConsts.search.topicExchange);
    }

    /**
     * 文章同步至 ES 佇列
     */
    @Bean(value = RabbitMqConsts.search.syncArticleToES.queue)
    public Queue syncArticleToESQueue() {
        return new Queue(RabbitMqConsts.search.syncArticleToES.queue);
    }

    /**
     * 綁定佇列到交換機
     */
    @Bean
    public Binding syncArticleToESBinding(
            @Qualifier(RabbitMqConsts.search.syncArticleToES.queue) Queue queue,
            @Qualifier(RabbitMqConsts.search.topicExchange) TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.search.syncArticleToES.routingKey);
    }

    /**
     * JSON 消息轉換器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
