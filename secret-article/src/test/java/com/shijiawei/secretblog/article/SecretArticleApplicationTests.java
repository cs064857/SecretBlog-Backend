package com.shijiawei.secretblog.article;

import lombok.extern.slf4j.Slf4j;
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
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.time.Duration;
import java.util.Arrays;

//@SpringBootTest
@Slf4j
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
	@Test
	public void test2(){
		Integer categoryId=8;
		Integer routerPage=1;
		String keyExpression= "AmsArticles:categoryId_#{#categoryId}:routePage_#{#routePage}"; //正確,使用ParserContext.TEMPLATE_EXPRESSION,未使用則失敗
		//上述結論若使用ParserContext.TEMPLATE_EXPRESSION需要#{#變量}

//		String keyExpression= "'AmsArticles:categoryId_' + #categoryId +':routePage_' + #routePage";//正確,不使用ParserContext.TEMPLATE_EXPRESSION
		//上述結論若不使用ParserContext.TEMPLATE_EXPRESSION需要#變量,並且使用連接符號,不需要{與}


		EvaluationContext context=new StandardEvaluationContext();

		context.setVariable("categoryId",categoryId);
		context.setVariable("routePage",routerPage);
		log.info("context:{}",context);
		SpelExpressionParser parser = new SpelExpressionParser();
		String key = parser.parseExpression(keyExpression,ParserContext.TEMPLATE_EXPRESSION).getValue(context, String.class);
		log.info("key:{}",key);
	}
}
