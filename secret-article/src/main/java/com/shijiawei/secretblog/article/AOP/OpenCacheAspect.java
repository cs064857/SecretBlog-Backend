package com.shijiawei.secretblog.article.AOP;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.article.annotation.OpenCache;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: OpenCacheAspect
 * Description:
 *
 * @Create 2024/9/5 下午3:26
 */
@Component
@Aspect
@Slf4j
public class OpenCacheAspect {
    @Autowired
    private RedissonClient redissonClient;

    @Around("@annotation(com.shijiawei.secretblog.article.annotation.OpenCache)")
    public Object openCacheAspect(ProceedingJoinPoint joinPoint) throws Throwable {
        //獲得原方法簽名
        MethodSignature methodSignature = (MethodSignature)joinPoint.getSignature();
        //獲得原方法
        Method method = methodSignature.getMethod();
        //從原方法上獲得註解
        OpenCache openCache = method.getAnnotation(OpenCache.class);
        //從方法上的註解獲取prefix、key並組合成最終key
        String key = openCache.prefix()+":"+openCache.key();


        //試圖從Redis緩存中獲得文章列表數據
        String redisCache = (String) redissonClient.getBucket(key).get();
        //若成功獲得文章列表數據
        if (redisCache!=null) {
            Object obj = JacksonUtils.toObj(redisCache, method.getGenericReturnType());//將String轉換成原方法中的返回值類型
            log.info("緩存命中...");
            return obj;
        }

        Object proceed = new Object();

        //若Redis沒有緩存該資料須查詢資料時,加上分散式鎖,只放一名用戶進入資料庫中查詢,解決緩存擊穿問題
        if(redissonClient.getLock(key+"_Lock").tryLock()){
            try {
                log.info("獲得Redisson分散式鎖...");
                //查詢資料庫前再次嘗試從Redis緩存中取得數據
                redisCache = (String) redissonClient.getBucket(key).get();
                //若成功獲得文章列表數據
                if (redisCache!=null) {
                    Object obj = JacksonUtils.toObj(redisCache, method.getGenericReturnType());//將String轉換成原方法中的返回值類型
                    log.info("分散式鎖中緩存命中...");
                    return obj;
                }

                log.info("查詢資料庫中...");
                //執行原方法代碼,若Redis緩存中未存在該數據,則至資料庫中查詢,並保存至Redis緩存中
               proceed = joinPoint.proceed();
                if (proceed!=null) {
                    //從方法上的註解獲取緩存時間資訊
                    Duration duration = Duration.of(openCache.time(), openCache.chronoUnit());//默認為24,ChronoUnit.HOURS
                    //將資料保存至Redis緩存中,默認過期時間為24小時
                    redissonClient.getBucket(key).setAsync(JacksonUtils.toJson(proceed),duration);
                    return proceed;
                }
            } finally {
                //強制解鎖
                redissonClient.getLock(key+"_Lock").forceUnlock();
            }
        }
        return null;
    }
}
