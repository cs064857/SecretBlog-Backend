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

    @Bean(value = RabbitMqConsts.user.userAvatarUpdate.queue)
    public Queue userAvatarUpdateQueue() {
        return new Queue(RabbitMqConsts.user.userAvatarUpdate.queue);
    }

    @Bean(value = RabbitMqConsts.user.topicExchange)
    public TopicExchange userTopicDirectExchange() {
        return new TopicExchange(RabbitMqConsts.user.topicExchange);
    }

    @Bean
    public Binding bindCommentActionQueueToTopicExchange(
            @Qualifier(value = RabbitMqConsts.user.userAvatarUpdate.queue)Queue queue,
            @Qualifier(value = RabbitMqConsts.user.topicExchange) TopicExchange exchange)
    {

        System.out.println("Spring 正在執行這個方法2");
        return BindingBuilder.bind(queue).to(exchange).with(RabbitMqConsts.user.userAvatarUpdate.routingKey);
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

