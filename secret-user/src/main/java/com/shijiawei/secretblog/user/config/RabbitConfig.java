package com.shijiawei.secretblog.user.config;


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
     * 死信隊列相關
     */

    //用於接收所有死亡的訊息並處理日誌相關的Queue
    @Bean(value = RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
    public TopicExchange logsDeadLetterExchange(){

        return new TopicExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE);


    }

    //用於接收所有死亡的訊息並處理日誌相關的Queue
    @Bean(value = RabbitMqConsts.User.DEAD_LETTER_QUEUE)
    public Queue logsDeadLetterQueue(){

        return QueueBuilder.durable(RabbitMqConsts.User.DEAD_LETTER_QUEUE).build();

    }

    @Bean
    public Binding bindDeadLetterQueueToExchange(
            @Qualifier(value = RabbitMqConsts.User.DEAD_LETTER_QUEUE) Queue deadLetterQueue,
            @Qualifier(value = RabbitMqConsts.User.DEAD_LETTER_EXCHANGE) TopicExchange deadLetterExchange) {

        //將死信隊列綁定到死信交換機，並使用指定的Routing Key
        return BindingBuilder.bind(deadLetterQueue)
                .to(deadLetterExchange)
                .with(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY);
    }

    /**
     * 業務相關
     */

    @Bean(value = RabbitMqConsts.User.UserAvatarUpdate.QUEUE)
    public Queue userAvatarUpdateQueue() {
//        return new Queue(RabbitMqConsts.User.UserAvatarUpdate.QUEUE);
        return QueueBuilder.durable(RabbitMqConsts.User.UserAvatarUpdate.QUEUE)
                .deadLetterExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean(value = RabbitMqConsts.User.ArticleLikedEmailNotify.QUEUE)
    public Queue articleLikedEmailNotifyQueue() {
        return QueueBuilder.durable(RabbitMqConsts.User.ArticleLikedEmailNotify.QUEUE)
                .deadLetterExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean(value = RabbitMqConsts.User.ArticleRepliedNotify.QUEUE)
    public Queue articleRepliedEmailNotifyQueue() {
        return QueueBuilder.durable(RabbitMqConsts.User.ArticleRepliedNotify.QUEUE)
                .deadLetterExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean(value = RabbitMqConsts.User.ArticleRepliedInboxNotify.QUEUE)
    public Queue articleRepliedInboxNotifyQueue() {
        return QueueBuilder.durable(RabbitMqConsts.User.ArticleRepliedInboxNotify.QUEUE)
                .deadLetterExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean(value = RabbitMqConsts.User.CommentRepliedEmailNotify.QUEUE)
    public Queue commentRepliedEmailNotifyQueue() {
        return QueueBuilder.durable(RabbitMqConsts.User.CommentRepliedEmailNotify.QUEUE)
                .deadLetterExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean(value = RabbitMqConsts.User.CommentRepliedInboxNotify.QUEUE)
    public Queue commentRepliedInboxNotifyQueue() {
        return QueueBuilder.durable(RabbitMqConsts.User.CommentRepliedInboxNotify.QUEUE)
                .deadLetterExchange(RabbitMqConsts.User.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(RabbitMqConsts.User.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean(value = RabbitMqConsts.User.TOPIC_EXCHANGE)
    public TopicExchange userTopicDirectExchange() {
        return new TopicExchange(RabbitMqConsts.User.TOPIC_EXCHANGE);
    }

    @Bean
    public Binding bindCommentActionQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.User.UserAvatarUpdate.QUEUE)Queue queue,
            @Qualifier(value = RabbitMqConsts.User.TOPIC_EXCHANGE) TopicExchange exchange)
    {

        System.out.println("Spring 正在執行這個方法2");
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.User.UserAvatarUpdate.ROUTING_KEY);
    }

    @Bean
    public Binding bindArticleLikedEmailNotifyQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.User.ArticleLikedEmailNotify.QUEUE) Queue queue,
            @Qualifier(value = RabbitMqConsts.User.TOPIC_EXCHANGE) TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.User.ArticleLikedEmailNotify.ROUTING_KEY);
    }

    @Bean
    public Binding bindArticleRepliedEmailNotifyQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.User.ArticleRepliedNotify.QUEUE) Queue queue,
            @Qualifier(value = RabbitMqConsts.User.TOPIC_EXCHANGE) TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.User.ArticleRepliedNotify.ROUTING_KEY);
    }

    @Bean
    public Binding bindArticleRepliedInboxNotifyQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.User.ArticleRepliedInboxNotify.QUEUE) Queue queue,
            @Qualifier(value = RabbitMqConsts.User.TOPIC_EXCHANGE) TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.User.ArticleRepliedInboxNotify.ROUTING_KEY);
    }

    @Bean
    public Binding bindCommentRepliedEmailNotifyQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.User.CommentRepliedEmailNotify.QUEUE) Queue queue,
            @Qualifier(value = RabbitMqConsts.User.TOPIC_EXCHANGE) TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.User.CommentRepliedEmailNotify.ROUTING_KEY);
    }

    @Bean
    public Binding bindCommentRepliedInboxNotifyQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.User.CommentRepliedInboxNotify.QUEUE) Queue queue,
            @Qualifier(value = RabbitMqConsts.User.TOPIC_EXCHANGE) TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.User.CommentRepliedInboxNotify.ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        // 使用 JSON 轉換器替代預設的 SimpleMessageConverter
        return new Jackson2JsonMessageConverter();
    }
//
//    @Bean
//    public FanoutExchange commentActionExchange() {
//        return new FanoutExchange("Auth Notification FanoutExchange");
//    }
//
//
//
//    @Bean
//    public Binding bindNotificationQueueToDirectExchange(Queue queue, DirectExchange commentActionDirectExchange) {
//        //只有當Routing Key為"auth.notification"時，Queue才會收到
//        System.out.println("🔥 Spring 正在執行這個方法2");
//        return BindingBuilder.bind(queue).to(commentActionDirectExchange).with("auth.notification");
//    }


}
