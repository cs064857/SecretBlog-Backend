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

    @Bean(value = RabbitMqConsts.User.UserAvatarUpdate.QUEUE)
    public Queue userAvatarUpdateQueue() {
        return new Queue(RabbitMqConsts.User.UserAvatarUpdate.QUEUE);
    }

    @Bean(value = RabbitMqConsts.User.ArticleLikedEmailNotify.QUEUE)
    public Queue articleLikedEmailNotifyQueue() {
        return new Queue(RabbitMqConsts.User.ArticleLikedEmailNotify.QUEUE);
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
