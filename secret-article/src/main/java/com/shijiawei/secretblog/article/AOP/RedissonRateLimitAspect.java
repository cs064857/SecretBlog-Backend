//package com.shijiawei.secretblog.article.AOP;
//
//import com.shijiawei.secretblog.common.annotation.RedissonRateLimit;
//import com.shijiawei.secretblog.common.myenum.RedisRateLimitKey;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.Signature;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.redisson.api.RedissonClient;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//
///**
// * ClassName: RedissonRateLimitAspect
// * Description:
// *
// * @Create 2025/10/8 上午2:49
// */
//@Aspect
//@Component
//@Slf4j
//public class RedissonRateLimitAspect{
//
//    private final RedissonClient redissonClient;
//
//    public RedissonRateLimitAspect (RedissonClient redissonClient, RedissonClient redissonClient1){
//        this.redissonClient = redissonClient1;
//    }
//
//
//    @Around("@annotation(redissonRateLimit)")
//    public Object redissonRateLimitAspect(ProceedingJoinPoint proceedingJoinPoint, RedissonRateLimit redissonRateLimit) {
//        RedisRateLimitKey rateLimitKey = redissonRateLimit.rateLimitKey();
//        String value = redissonRateLimit.value();
//        String messages = redissonRateLimit.messages();
//        log.debug("RedisRateLimitKey: {}", rateLimitKey);
//        log.debug("Value: {}", value);
//        log.debug("Messages: {}", messages);
//
//            // 透過切面簽名獲取到註解中的Redis快取儲存時的前綴、鍵值、時間單位等資訊
////        //從切面中獲取方法的簽名
//
//
////        MethodSignature signature = (MethodSignature)proceedingJoinPoint.getSignature();
////        Method method = signature.getMethod();
////        RedissonRateLimit annotation = method.getAnnotation(RedissonRateLimit.class);
////        String key = annotation.key();
////        String value = annotation.value();
////        String messages = annotation.messages();
//
//        return null;
//    }
//
//
//}
