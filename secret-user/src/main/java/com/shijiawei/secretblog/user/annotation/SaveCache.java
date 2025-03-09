package com.shijiawei.secretblog.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * 自定義快取儲存註解（用於標記需要儲存到快取的方法，不會查詢已存在的快取）
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SaveCache {
    String prefix() default "";     // 快取鍵前綴
    String key() default "";        // 快取鍵主體（支持 SpEL 表達式）
    int time() default 24;          // 快取時間數值
    ChronoUnit chronoUnit() default ChronoUnit.HOURS; // 時間單位
} 