package com.shijiawei.secretblog.article.config;


import com.shijiawei.secretblog.article.entity.AmsArtStatus;
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
     * 1、用戶對文章互動行為更新（點讚/取消點讚狀態）
     * 2、用戶對文章互動行為更新（點讚/取消點讚狀態）
     * 共用一把RoutingKey
     */


    //文章讚數更新佇列 (點讚/取消點讚狀態同步到 AmsArtStatus)
    @Bean(value = RabbitMqConsts.Ams.UpdateArticleLiked.QUEUE)
    public Queue updateArticleLikedQueue(){
        return new Queue(RabbitMqConsts.Ams.UpdateArticleLiked.QUEUE);
    }
    //文章互動行為更新佇列 (點讚/取消點讚狀態同步到 AmsArtAction)

    @Bean(value = RabbitMqConsts.Ams.UpdateArticleAction.QUEUE)
    public Queue updateArticleActionQueue(){
        return new Queue(RabbitMqConsts.Ams.UpdateArticleAction.QUEUE);
    }
    @Bean
    public Binding amsBinding(@Qualifier(value = RabbitMqConsts.Ams.UpdateArticleLiked.QUEUE) Queue queue,
                              @Qualifier(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Ams.ArticleLikeChanged.ROUTING_KEY);
    }

    @Bean
    public Binding amsActionBinding(@Qualifier(value = RabbitMqConsts.Ams.UpdateArticleAction.QUEUE) Queue queue,
                                    @Qualifier(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Ams.ArticleLikeChanged.ROUTING_KEY);
    }


    /**
     * 文章服務 Topic Exchange
     */
    @Bean(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE)
    public TopicExchange amsTopicExchange(){
        return new TopicExchange(RabbitMqConsts.Ams.TOPIC_EXCHANGE);
    }

    /**
     * 搜索服務 Topic Exchange（用於發送 ES 同步消息）
     */
    @Bean(value = RabbitMqConsts.Search.TOPIC_EXCHANGE)
    public TopicExchange searchTopicExchange(){
        return new TopicExchange(RabbitMqConsts.Search.TOPIC_EXCHANGE);
    }

    /**
     * 使用者服務 Topic Exchange
     */
    @Bean(value = RabbitMqConsts.User.TOPIC_EXCHANGE)
    public TopicExchange userTopicExchange() {
        return new TopicExchange(RabbitMqConsts.User.TOPIC_EXCHANGE);
    }





    /**
     * 文章書籤數更新佇列 (書籤數同步到 AmsArtStatus)
     */
    @Bean(value = RabbitMqConsts.Ams.UpdateArticleBookmark.QUEUE)
    public Queue updateArticleBookmarkQueue(){
        return new Queue(RabbitMqConsts.Ams.UpdateArticleBookmark.QUEUE);
    }

    @Bean
    public Binding amsBookmarkBinding(@Qualifier(value = RabbitMqConsts.Ams.UpdateArticleBookmark.QUEUE) Queue queue,
                                      @Qualifier(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Ams.UpdateArticleBookmark.ROUTING_KEY);
    }

    /**
     * 文章書籤行為更新佇列 (加入/移除書籤狀態同步到 AmsArtAction)
     */
    @Bean(value = RabbitMqConsts.Ams.UpdateArticleBookmarkAction.QUEUE)
    public Queue updateArticleBookmarkActionQueue(){
        return new Queue(RabbitMqConsts.Ams.UpdateArticleBookmarkAction.QUEUE);
    }

    @Bean
    public Binding amsBookmarkActionBinding(@Qualifier(value = RabbitMqConsts.Ams.UpdateArticleBookmarkAction.QUEUE) Queue queue,
                                            @Qualifier(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Ams.UpdateArticleBookmarkAction.ROUTING_KEY);
    }

    /**
     * 留言讚數更新佇列 (點讚數同步到 AmsCommentStatistics)
     */
    @Bean(value = RabbitMqConsts.Ams.UpdateCommentLiked.QUEUE)
    public Queue updateCommentLikedQueue(){
        return new Queue(RabbitMqConsts.Ams.UpdateCommentLiked.QUEUE);
    }

    @Bean
    public Binding amsCommentLikedBinding(@Qualifier(value = RabbitMqConsts.Ams.UpdateCommentLiked.QUEUE) Queue queue,
                                          @Qualifier(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Ams.UpdateCommentLiked.ROUTING_KEY);
    }

    /**
     * 留言互動行為更新佇列 (點讚/取消點讚狀態同步到 AmsCommentAction)
     */
    @Bean(value = RabbitMqConsts.Ams.UpdateCommentAction.QUEUE)
    public Queue updateCommentActionQueue(){
        return new Queue(RabbitMqConsts.Ams.UpdateCommentAction.QUEUE);
    }

    @Bean
    public Binding amsCommentActionBinding(@Qualifier(value = RabbitMqConsts.Ams.UpdateCommentAction.QUEUE) Queue queue,
                                           @Qualifier(value = RabbitMqConsts.Ams.TOPIC_EXCHANGE) TopicExchange topicExchange){
        return BindingBuilder.bind(queue).to(topicExchange).with(RabbitMqConsts.Ams.UpdateCommentAction.ROUTING_KEY);
    }


    @Bean
    public MessageConverter bindAmsLikedUpdatedQueueToExchange() {
        // 使用 JSON 轉換器替代預設的 SimpleMessageConverter
        return new Jackson2JsonMessageConverter();
    }
}
