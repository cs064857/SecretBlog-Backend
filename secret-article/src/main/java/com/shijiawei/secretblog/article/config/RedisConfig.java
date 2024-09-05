//package com.shijiawei.secretblog.article.config;
//
//import com.alibaba.nacos.shaded.com.google.protobuf.JavaType;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//
///**
// * ClassName: RedisConfig
// * Description:
// *
// * @Create 2024/9/4 下午3:43
// */
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
//
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<Object>(Object.class));
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
//}
