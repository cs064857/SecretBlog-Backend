package com.shijiawei.secretblog.article.AOP;

import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RKeys;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@Aspect
public class DelayDoubleDeleteAspect {

    @Autowired
    RedissonClient redissonClient;

    final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(com.shijiawei.secretblog.article.annotation.DelayDoubleDelete)")
    public Object delayDoubleDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        com.shijiawei.secretblog.article.annotation.DelayDoubleDelete annotation = methodSignature.getMethod().getAnnotation(com.shijiawei.secretblog.article.annotation.DelayDoubleDelete.class);

        String keyExpression = annotation.key();
        if(keyExpression.contains("#")){
            keyExpression = generateKey(keyExpression,joinPoint);
        }
        String keyPrefix = annotation.prefix() + ":" + keyExpression;

        // 修改這裡：使用模式匹配刪除所有相關的鍵
        long deletedCount = deleteKeysWithPrefix(keyPrefix);
        log.info("首次刪除({})快取數據，共刪除{}個鍵", keyPrefix, deletedCount);

        Object proceed = joinPoint.proceed();
        scheduleDelayedCacheDeletion(keyPrefix, annotation.delay(), annotation.timeUnit());
        return proceed;
    }

    private long deleteKeysWithPrefix(String keyPrefix) {
        RKeys keys = redissonClient.getKeys();
        Iterable<String> keysToDelete = keys.getKeysByPattern(keyPrefix + "*");
        long count = 0;
        for (String key : keysToDelete) {
            redissonClient.getBucket(key).delete();
            count++;
        }
        return count;
    }

    public void scheduleDelayedCacheDeletion(String keyPrefix, long delay, TimeUnit timeUnit){
        String queueKey = RedisCacheKey.CACHE_DELETION_QUEUE.format(keyPrefix);
        RQueue<Object> cacheDeletionQueue = redissonClient.getQueue(queueKey);
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(cacheDeletionQueue);
        delayedQueue.offer(keyPrefix, delay, timeUnit);

        new Thread(() -> {
            while (true) {
                Object poll = cacheDeletionQueue.poll();
                if (poll != null) {
                    long deletedCount = deleteKeysWithPrefix(keyPrefix);
                    log.info("延遲雙刪完畢...刪除前綴:{}，共刪除{}個鍵", poll, deletedCount);
                    break;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public String generateKey(String keyExpression, ProceedingJoinPoint joinPoint){
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }
        return parser.parseExpression(keyExpression, ParserContext.TEMPLATE_EXPRESSION).getValue(context, String.class);
}


    }
