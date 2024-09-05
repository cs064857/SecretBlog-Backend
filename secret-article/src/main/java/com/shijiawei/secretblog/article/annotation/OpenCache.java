package com.shijiawei.secretblog.article.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: OpenCache
 * Description:
 *
 * @Create 2024/9/5 下午3:25
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenCache {
    String prefix() default "";
    String key() default "";
    int time() default 24;
    ChronoUnit chronoUnit() default ChronoUnit.HOURS;
}
