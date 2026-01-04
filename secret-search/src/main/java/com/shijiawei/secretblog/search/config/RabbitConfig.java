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
    @Bean(value = RabbitMqConsts.Search.TOPIC_EXCHANGE)
    public TopicExchange searchTopicExchange() {
        return new TopicExchange(RabbitMqConsts.Search.TOPIC_EXCHANGE);
    }

    /**
     * 文章同步至 ES 佇列
     */
    @Bean(value = RabbitMqConsts.Search.SyncArticleToES.QUEUE)
    public Queue syncArticleToESQueue() {
        return new Queue(RabbitMqConsts.Search.SyncArticleToES.QUEUE);
    }

    /**
     * 綁定佇列到交換機
     */
    @Bean
    public Binding syncArticleToESBinding(
            @Qualifier(RabbitMqConsts.Search.SyncArticleToES.QUEUE) Queue queue,
            @Qualifier(RabbitMqConsts.Search.TOPIC_EXCHANGE) TopicExchange topicExchange) {
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Search.SyncArticleToES.ROUTING_KEY);
    }

    /**
     * JSON 消息轉換器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
