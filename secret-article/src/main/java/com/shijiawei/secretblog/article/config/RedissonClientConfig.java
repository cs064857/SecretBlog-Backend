package com.shijiawei.secretblog.article.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.spring.starter.RedissonProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * ClassName: RedissonClientConfig
 * Description:
 *
 * @Create 2024/9/4 下午4:06
 */
@Configuration
@EnableConfigurationProperties(RedissonProperties.class)
public class RedissonClientConfig {

    @Autowired
    private RedissonProperties redissonProperties;

    @Bean
    public RedissonClient redissonClient() {

        // 創建一個Config對象來設置Redisson的配置
        Config config = new Config();
        // 連接到Redis的基本URL或其他參數
        config.useSingleServer()
                .setAddress("redis://192.168.91.133:6379");


        config.setCodec(StringCodec.INSTANCE);// 設置Redisson使用String進行序列化和反序列化
//        config.setCodec(new JsonJacksonCodec());// 設置Redisson使用JsonJacksonCodec進行序列化和反序列化

        // 根據配置創建RedissonClient
        return Redisson.create(config);
    }
}
