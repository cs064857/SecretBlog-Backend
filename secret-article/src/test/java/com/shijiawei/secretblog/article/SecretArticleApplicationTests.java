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
import java.util.Arrays;

//@SpringBootTest
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
	@Test
	public void test(){
		String str = "abcde"+"#categoryId"+"weqe"+"#page";
		String[] split = Arrays.stream(str.split("(?=#)"))
				.filter(s -> s.startsWith("#"))
				.toArray(String[]::new);
//		for (String s : split) {
//			System.out.println(s);
//		}
	}
	@Test
	public void test1(){
		String prefix = "AmsArticles_category:#{categoryId}_#{routerPage}";

		String[] split = Arrays.stream(prefix.split("(?=#)"))  // 以 # 進行分割
				.map(s -> s.replaceAll("\\{(.*?)}.*", "$1")) // 提取 {} 中的文字，清空 } 後的字，保留 #
				.filter(s -> s.startsWith("#"))                // 確保保留以 # 開頭的數組
				.toArray(String[]::new);                       // 轉換成 String 陣列

		String[] split2 = Arrays.stream(prefix.split("(?=#)"))  // 以 # 進行分割
				.map(s -> s.replaceAll("(#\\{.*?})[^}]*", "$1"))
				.filter(s -> s.startsWith("#"))                // 確保保留以 # 開頭的數組
				.toArray(String[]::new);                       // 轉換成 String 陣列
		System.out.println();
	}
}
