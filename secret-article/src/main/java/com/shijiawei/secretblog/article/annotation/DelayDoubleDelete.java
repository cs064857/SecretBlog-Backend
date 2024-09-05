package com.shijiawei.secretblog.article.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: DelayDoubleDeleteAspect
 * Description: Redisson開啟延遲雙刪
 *
 * @Create 2024/9/5 上午1:03
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface DelayDoubleDelete {
    String key() default "";
    int delay() default 5;
    TimeUnit timeUnit() default TimeUnit.SECONDS;
}
