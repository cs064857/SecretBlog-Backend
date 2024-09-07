package com.shijiawei.secretblog.article.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * ClassName: DelayDoubleDeleteAspect
 * Description:
 *
 * @Create 2024/9/5 上午1:11
 */
@Component
@Slf4j
@Aspect
public class DelayDoubleDeleteAspect {

    @Autowired
    RedissonClient redissonClient;

    final SpelExpressionParser parser = new SpelExpressionParser();
    /**
     * Redisson延遲雙刪
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.shijiawei.secretblog.article.annotation.DelayDoubleDelete)")
    public Object delayDoubleDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        //獲取被AOP代理的方法簽名
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        //透過方法簽名獲取方法上的註解
        com.shijiawei.secretblog.article.annotation.DelayDoubleDelete annotation = methodSignature.getMethod().getAnnotation(com.shijiawei.secretblog.article.annotation.DelayDoubleDelete.class);

        String keyExpression = annotation.key();
        //若keyExpression中包含SpEL表達式的話
        if(keyExpression.contains("#")){
            keyExpression = generateKey(keyExpression,joinPoint);
        }
        //組合成最終key
        String key = annotation.prefix()+":"+keyExpression;

        //初次刪除在Redis中的緩存,避免再新增時有人讀取緩存數據
        redissonClient.getBucket(key).delete();
        //執行原代碼
        Object proceed = joinPoint.proceed();
        scheduleDelayedCacheDeletion(key,annotation.delay(),annotation.timeUnit());//時間由註解定義,默認為5秒
        return proceed;
    }


    public void scheduleDelayedCacheDeletion(String key, long delay, TimeUnit timeUnit){
        //創建普通隊列
        RQueue<Object> cacheDeletionQueue = redissonClient.getQueue("cacheDeletionQueue:"+key);
        //創建延遲隊列並將普通隊列放置於其中
        RDelayedQueue<Object> delayedQueue = redissonClient.getDelayedQueue(cacheDeletionQueue);
        //到達延遲時間時,將key作為Value放入延遲隊列中,key為"cacheDeletionQueue:"+key,value為key
        delayedQueue.offer(key,delay,timeUnit);

        //自動刪除該緩存鍵
        // 模擬阻塞等待
        new Thread(() -> {
            while (true) {
                Object poll = cacheDeletionQueue.poll();
                if (poll != null) {
                    redissonClient.getBucket(key).delete();
                    log.info("延遲雙刪完畢...刪除鍵:{}", poll);
                    break;
                }
                try {
                    Thread.sleep(100); // 等待 100 毫秒再嘗試
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

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}
