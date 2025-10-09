//package com.shijiawei.secretblog.common.annotation;
//
//import com.shijiawei.secretblog.common.feign.RateLimitKey;
//
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//
///**
// * ClassName: RedissonRateLimit
// * Description:
// *
// * @Create 2025/10/8 上午2:42
// */
//@Target({ElementType.METHOD})
//@Retention(RetentionPolicy.RUNTIME)
//public @interface RedissonRateLimit {
//    RateLimitKey rateLimitKey();
//    String value();
//    String messages() default "操作過於頻繁，請稍後再試";
//}
