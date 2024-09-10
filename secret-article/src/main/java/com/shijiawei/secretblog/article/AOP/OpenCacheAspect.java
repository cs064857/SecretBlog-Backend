// 導入必要的類和接口
package com.shijiawei.secretblog.article.AOP;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.shijiawei.secretblog.article.annotation.OpenCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
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

/**
 * 使用範例：@OpenCache(prefix = "AmsArticles", key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:articles")//正確SpEL語法,變數使用#{#變數名}
 */
@Component
@Aspect
@Slf4j
public class OpenCacheAspect {

    // @Autowired 註解表示 Spring 會自動注入 RedissonClient 的實例
    // RedissonClient 是用於與 Redis 進行交互的客戶端
    @Autowired
    private RedissonClient redissonClient;

    // ExpressionParser 用於解析 SpEL（Spring Expression Language）表達式
    private final ExpressionParser parser = new SpelExpressionParser();

    // DefaultParameterNameDiscoverer 用於獲取方法參數的名稱
    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    // @Around 註解定義了一個環繞通知，它會在被 @OpenCache 註解標記的方法執行前後運行
    // 這個方法實現了緩存的核心邏輯
    @Around("@annotation(com.shijiawei.secretblog.article.annotation.OpenCache)")
    public Object openCacheAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        // 獲取被調用方法的簽名信息
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 獲取被調用的方法
        Method method = methodSignature.getMethod();
        // 獲取方法上的 @OpenCache 註解
        OpenCache openCache = method.getAnnotation(OpenCache.class);

        // 從註解中獲取緩存的前綴
        String prefix = openCache.prefix();


        // 從註解中獲取緩存的 key 表達式
        String keyExpression = openCache.key();

        // 記錄方法名和 key 表達式，用於調試
        log.debug("Method: {}", method.getName());
        log.debug("Key expression: {}", keyExpression);

        //若包含SpEL表達示
        if(keyExpression.contains("#")){
            // 生成緩存的 key
            keyExpression = generateKey(keyExpression, method, joinPoint.getArgs());
        }

        String key = prefix + ":" + keyExpression;

        // 記錄生成的緩存 key
        log.info("Generated cache key: {}", key);

        // 嘗試從 Redis 中獲取緩存數據
        String redisCache = (String) redissonClient.getBucket(key).get();
        if (redisCache != null) {
            log.info("緩存命中...");
            // 如果緩存命中，將緩存的 JSON 字符串轉換為對象並返回
            return JacksonUtils.toObj(redisCache, method.getGenericReturnType());
        }

        // 如果緩存未命中，嘗試獲取分佈式鎖
        if (redissonClient.getLock(key + "_Lock").tryLock()) {
            try {
                log.info("獲得Redisson分散式鎖...");
                // 再次檢查緩存，防止其他線程已經更新了緩存
                redisCache = (String) redissonClient.getBucket(key).get();
                if (redisCache != null) {
                    log.info("分散式鎖中緩存命中...");
                    return JacksonUtils.toObj(redisCache, method.getGenericReturnType());
                }

                log.info("查詢資料庫中...");
                // 執行原方法，通常是查詢數據庫
                Object proceed = joinPoint.proceed();
                if (proceed != null) {
                    // 計算緩存的過期時間
                    Duration duration = Duration.of(openCache.time(), openCache.chronoUnit());
                    // 將結果存入 Redis 緩存
                    redissonClient.getBucket(key).setAsync(JacksonUtils.toJson(proceed), duration);
                    return proceed;
                }
            } finally {
                // 確保在方法執行結束後釋放鎖
                redissonClient.getLock(key + "_Lock").unlock();
            }
        }
        return null;
    }

    // 這個私有方法用於生成緩存的 key
    private String generateKey(String keyExpression, Method method, Object[] args) {
        // 創建 SpEL 表達式的評估上下文
        EvaluationContext context = new StandardEvaluationContext();
        // 獲取方法的參數名
        String[] parameterNames = nameDiscoverer.getParameterNames(method);

        if (parameterNames == null) {
            log.error("無法獲取方法參數名稱");
            return keyExpression;
        }

        // 記錄方法參數名和參數值，用於調試
        log.debug("Method parameters: {}", Arrays.toString(parameterNames));
        log.debug("Method arguments: {}", Arrays.toString(args));

        // 將方法參數添加到 SpEL 上下文中
        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        try {
            // 解析 SpEL 表達式並返回結果
            /**
             * const中的variables的size有2個,[0]=key="routePage",value=2 & [1]=key="categoryId",value=2
             * "'categoryId_' + #categoryId + ':routerPage_' + #routePage + ':articles'"經過parser之後
             * 變成
             * "categoryId_2:routerPage_2:articles"
             */
            return parser.parseExpression(keyExpression, ParserContext.TEMPLATE_EXPRESSION).getValue(context, String.class);
        } catch (Exception e) {
            // 如果解析失敗，記錄錯誤並返回原始表達式
            log.error("解析key表達式時出錯: {}", e.getMessage());
            return keyExpression;
        }
    }
}