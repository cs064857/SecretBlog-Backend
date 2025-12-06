package com.shijiawei.secretblog.article.config;


import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
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


    /**
     * 文章讚數更新佇列 (點讚/取消點讚狀態同步到 AmsArtStatus)
     */
    @Bean(value = RabbitMqConsts.ams.updateArticleLiked.queue)
    public Queue updateArticleLikedQueue(){
        return new Queue(RabbitMqConsts.ams.updateArticleLiked.queue);
    }

    @Bean(value = RabbitMqConsts.ams.topicExchange)
    public TopicExchange amsTopicExchange(){
        return new TopicExchange(RabbitMqConsts.ams.topicExchange);
    }

    @Bean
    public Binding amsBinding(@Qualifier(value = RabbitMqConsts.ams.updateArticleLiked.queue) Queue queue,
                              @Qualifier(value = RabbitMqConsts.ams.topicExchange) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.ams.updateArticleLiked.routingKey);
    }

    /**
     * 文章互動行為更新佇列 (點讚/取消點讚狀態同步到 AmsArtAction)
     */
    @Bean(value = RabbitMqConsts.ams.updateArticleAction.queue)
    public Queue updateArticleActionQueue(){
        return new Queue(RabbitMqConsts.ams.updateArticleAction.queue);
    }

    @Bean
    public Binding amsActionBinding(@Qualifier(value = RabbitMqConsts.ams.updateArticleAction.queue) Queue queue,
                                    @Qualifier(value = RabbitMqConsts.ams.topicExchange) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.ams.updateArticleAction.routingKey);
    }


    @Bean
    public MessageConverter bindAmsLikedUpdatedQueueToExchange() {
        // 使用 JSON 轉換器替代預設的 SimpleMessageConverter
        return new Jackson2JsonMessageConverter();
    }
}
