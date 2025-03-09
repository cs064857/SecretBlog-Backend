package com.shijiawei.secretblog.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * ClassName: OpenCache
 * Description:
 * 使用範例：
 * @OpenCache(prefix = "AmsCategory",key = "treeCategoryVos",time = 30,chronoUnit = ChronoUnit.MINUTES)
 * 或
 * OpenCache(prefix = "AmsArticles", key = "categoryId_#{#categoryId}:routerPage_#{#routePage}:articles")
 * @Create 2024/9/5 下午3:25
 */

/**
 * 自定義快取註解（用於標記需要快取的方法）
 */
@Target({ElementType.METHOD})  // 此註解只能應用在方法上
@Retention(RetentionPolicy.RUNTIME) // 註解信息在運行時保留（可通過反射讀取）
public @interface OpenCache {
    String prefix() default "";     // 快取鍵前綴（用於分類快取）
    String key() default "";        // 快取鍵主體（支持 SpEL 表達式，例如："user_#{#userId}"）
    int time() default 24;          // 快取時間數值（預設24）
    ChronoUnit chronoUnit() default ChronoUnit.HOURS; // 時間單位（預設小時）
}
