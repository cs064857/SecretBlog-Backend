package com.shijiawei.secretblog.article.AOP;


import com.alibaba.fastjson2.JSON;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessException;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 使用範例：@OpenCache(prefix = "AmsArticles", key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:articles")
 * // 正確 SpEL 語法, 變數使用 #{#變數名}
 */
@Component
@Aspect
@Slf4j
public class OpenCacheAspect {

    @Autowired
    private RedissonClient redissonClient;

    private final ExpressionParser parser = new SpelExpressionParser();
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    @Around("@annotation(com.shijiawei.secretblog.common.annotation.OpenCache)")
    public Object openCacheAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        /**
         * 透過切面簽名獲取到註解中的Redis快取儲存時的前綴、鍵值、時間單位等資訊
         */
        //從切面中獲取方法的簽名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //從方法的簽名獲取方法的對象
        Method method = methodSignature.getMethod();
        //從方法的對象中獲得註解的對象
        OpenCache openCache = method.getAnnotation(OpenCache.class);
        //從註解的對象中獲取註解中的資訊
        String prefix = openCache.prefix();
        String keyExpression = openCache.key();

        log.debug("Method: {}", method.getName());
        log.debug("Key expression: {}", keyExpression);

        // 若包含 SpEL 表達式
        // joinPoint.getArgs()得到具體的參數值，例如[0]為3415518(categoryId的值)、[1]為1(routePage的值)
        if (keyExpression.contains("#")) {
            keyExpression = generateKey(keyExpression, method, joinPoint.getArgs());
        }
        //從方法上的註解獲取prefix、key值並組合成最終key
        //例如：AmsArticles:categoryId_3415518L:routerPage_1:articles
        String key = prefix + ":" + keyExpression;
        log.info("Generated cache key: {}", key);
        //試圖從Redis緩存中獲得文章列表數據
        String redisCache = getRedisCache(key);
        //若成功獲得文章列表數據
        if (redisCache != null) {
            log.info("緩存命中...");
            // 使用 Fastjson 解析，將String轉換成原方法中的返回值類型
            return JSON.parseObject(redisCache, method.getGenericReturnType());
        }

        long waitTime = 5000;//最多等待多久來搶這個鎖
        long leaseTime = 10000;//搶到鎖後，多久會自動釋放

        String lockKey = key + "_Lock";
        RLock lock = redissonClient.getLock(lockKey);
        boolean isLocked = false;

        try {
            // 嘗試獲取鎖。如果在 waitTime 內有人釋放鎖，這裡會立即返回 true
            isLocked = lock.tryLock(waitTime, leaseTime, TimeUnit.MILLISECONDS);

            if (isLocked) {
                log.info("獲得 Redisson 分散式鎖 (或等待後獲得)...");
                // Double Check
                // 無論是第一個拿到的，還是等待後拿到的，都要先檢查 Redis
                // 如果是等待後拿到的，前一個人應該已經寫入 Cache 了
                redisCache = getRedisCache(key);
                if (redisCache != null) {
                    log.info("分散式鎖中緩存命中 (Double Check)...");
                    return JSON.parseObject(redisCache, method.getGenericReturnType());
                }
                // 真的沒有快取，查詢資料庫
                log.info("查詢資料庫中...");
                Object proceed = joinPoint.proceed();
                if (proceed != null) {
                    Duration duration = Duration.of(openCache.time(), openCache.chronoUnit());
                    redissonClient.getBucket(key).setAsync(JSON.toJSONString(proceed), duration);
                    return proceed;
                } else {
                    //防止快取穿透
                    redissonClient.getBucket(key).setAsync("", Duration.ofMinutes(5));
                    return null;
                }
            } else {
                // 等了 5 秒還是沒拿到鎖 (系統極度繁忙或 DB 死鎖)
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                        .data(Map.of("key", key))
                        .build();
            }
        } catch (InterruptedException e) {
            log.error("獲取鎖時被中斷", e);
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            // 解鎖
            if (isLocked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String getRedisCache(String key) {
        return (String) redissonClient.getBucket(key).get();
    }

    private String generateKey(String keyExpression, Method method, Object[] args) {
        /**
         * 範例：
         * keyExpression為：categoryId_#{#categoryId}:routerPage_#{#routePage}:articles
         * method為：openCacheAspect方法的對象
         * args為：[0]categoryId的值是3415518L、[1]routePage的值是1
         */

        EvaluationContext context = new StandardEvaluationContext();

        //找到SpEL表達式的參數名，例如parameterNames陣列中：[0]為categoryId、[1]為routePage
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        if (parameterNames == null) {
            log.error("無法獲取方法參數名稱");
            return keyExpression;
        }

        log.debug("Method parameters: {}", Arrays.toString(parameterNames));
        log.debug("Method arguments: {}", Arrays.toString(args));

        // parameterNames[i], args[i]組合再一起成為
        // 例如兩個組合：
        // 1、categoryId指向3415518L
        // 2、routePage指向1
        // 此時context包含以下內容
        // {
        //  "categoryId": 3415518L,
        //  "routePage": 1
        //}
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        try {
            /**
             * 合成最終鍵並返回，例如：
             * categoryId_3415518L:routerPage_1:articles
             *
             */
            return parser
                    .parseExpression(keyExpression, ParserContext.TEMPLATE_EXPRESSION)
                    .getValue(context, String.class);
        } catch (Exception e) {
            log.error("解析 key 表達式時出錯: {}", e.getMessage());
            return keyExpression;
        }
    }
}