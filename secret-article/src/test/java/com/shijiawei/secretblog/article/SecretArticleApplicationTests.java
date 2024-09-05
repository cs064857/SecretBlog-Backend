package com.shijiawei.secretblog.article;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@SpringBootTest
class SecretArticleApplicationTests {
	@Autowired
	private RedissonClient redissonClient;
	@Autowired
	private RedisTemplate<String, String> redisTemplate;
	@Test
	void RedissonClient() {
		redissonClient.getLock("articleLock").lock();
		RBucket<Object> article = redissonClient.getBucket("article");
		article.delete();
		article.set("Hello", Duration.ofSeconds(30));
		redissonClient.getLock("articleLock").unlock();
	}
	@Test
	void RedisTemplate() {
		redisTemplate.opsForValue().set("hello", "world");
	}
}
