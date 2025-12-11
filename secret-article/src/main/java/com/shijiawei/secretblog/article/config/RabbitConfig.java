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

    /**
     * 搜索服務 Topic Exchange（用於發送 ES 同步消息）
     */
    @Bean(value = RabbitMqConsts.search.topicExchange)
    public TopicExchange searchTopicExchange(){
        return new TopicExchange(RabbitMqConsts.search.topicExchange);
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

    /**
     * 文章書籤數更新佇列 (書籤數同步到 AmsArtStatus)
     */
    @Bean(value = RabbitMqConsts.ams.updateArticleBookmark.queue)
    public Queue updateArticleBookmarkQueue(){
        return new Queue(RabbitMqConsts.ams.updateArticleBookmark.queue);
    }

    @Bean
    public Binding amsBookmarkBinding(@Qualifier(value = RabbitMqConsts.ams.updateArticleBookmark.queue) Queue queue,
                                      @Qualifier(value = RabbitMqConsts.ams.topicExchange) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.ams.updateArticleBookmark.routingKey);
    }

    /**
     * 文章書籤行為更新佇列 (加入/移除書籤狀態同步到 AmsArtAction)
     */
    @Bean(value = RabbitMqConsts.ams.updateArticleBookmarkAction.queue)
    public Queue updateArticleBookmarkActionQueue(){
        return new Queue(RabbitMqConsts.ams.updateArticleBookmarkAction.queue);
    }

    @Bean
    public Binding amsBookmarkActionBinding(@Qualifier(value = RabbitMqConsts.ams.updateArticleBookmarkAction.queue) Queue queue,
                                            @Qualifier(value = RabbitMqConsts.ams.topicExchange) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.ams.updateArticleBookmarkAction.routingKey);
    }

    /**
     * 留言讚數更新佇列 (點讚數同步到 AmsCommentStatistics)
     */
    @Bean(value = RabbitMqConsts.ams.updateCommentLiked.queue)
    public Queue updateCommentLikedQueue(){
        return new Queue(RabbitMqConsts.ams.updateCommentLiked.queue);
    }

    @Bean
    public Binding amsCommentLikedBinding(@Qualifier(value = RabbitMqConsts.ams.updateCommentLiked.queue) Queue queue,
                                          @Qualifier(value = RabbitMqConsts.ams.topicExchange) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.ams.updateCommentLiked.routingKey);
    }

    /**
     * 留言互動行為更新佇列 (點讚/取消點讚狀態同步到 AmsCommentAction)
     */
    @Bean(value = RabbitMqConsts.ams.updateCommentAction.queue)
    public Queue updateCommentActionQueue(){
        return new Queue(RabbitMqConsts.ams.updateCommentAction.queue);
    }

    @Bean
    public Binding amsCommentActionBinding(@Qualifier(value = RabbitMqConsts.ams.updateCommentAction.queue) Queue queue,
                                           @Qualifier(value = RabbitMqConsts.ams.topicExchange) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.ams.updateCommentAction.routingKey);
    }


    @Bean
    public MessageConverter bindAmsLikedUpdatedQueueToExchange() {
        // 使用 JSON 轉換器替代預設的 SimpleMessageConverter
        return new Jackson2JsonMessageConverter();
    }
}
