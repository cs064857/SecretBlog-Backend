package com.shijiawei.secretblog.user.AOP;


import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;

import com.shijiawei.secretblog.common.myenum.RedisLockKey;
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

import com.alibaba.fastjson2.JSON;
import com.shijiawei.secretblog.common.annotation.OpenCache ;

import lombok.extern.slf4j.Slf4j;

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
        String lockKey = RedisLockKey.OPEN_CACHE_LOCK.getFormat(key);
        log.info("Generated cache key: {}", key);
        //試圖從Redis緩存中獲得文章列表數據
        String redisCache = (String) redissonClient.getBucket(key).get();
        //若成功獲得文章列表數據
        if (redisCache != null) {
            log.info("緩存命中...");
            // 使用 Fastjson 解析，將String轉換成原方法中的返回值類型
            return JSON.parseObject(redisCache, method.getGenericReturnType());
        }
        //若Redis沒有緩存該資料須查詢資料時,加上分散式鎖,只放一名用戶進入資料庫中查詢,解決緩存擊穿問題
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.tryLock()) {
            try {
                log.info("獲得 Redisson 分散式鎖...");
                // Double check
                //查詢資料庫前再次嘗試從Redis緩存中取得數據
                redisCache = (String) redissonClient.getBucket(key).get();
                //若成功獲得資料
                if (redisCache != null) {
                    log.info("分散式鎖中緩存命中...");
                    //將String轉換成原方法中的返回值類型並回傳
                    return JSON.parseObject(redisCache, method.getGenericReturnType());
                }

                log.info("查詢資料庫中...");
                // 執行原方法
                Object proceed = joinPoint.proceed();
                if (proceed != null) {

                    //從方法上的註解獲取緩存時間資訊,默認為24,ChronoUnit.HOURS
                    Duration duration = Duration.of(openCache.time(), openCache.chronoUnit());
                    // //將資料保存至Redis緩存中,默認過期時間為24小時,將結果寫入 Fastjson 字串
                    redissonClient.getBucket(key).setAsync(JSON.toJSONString(proceed), duration);
                    return proceed;
                }
            } finally {
                // 無論成功或失敗強制解鎖
                // 解鎖前檢查線程持有權
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return null;
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