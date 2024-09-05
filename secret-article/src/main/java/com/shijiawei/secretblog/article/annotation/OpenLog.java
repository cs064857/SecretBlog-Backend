package com.shijiawei.secretblog.article.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ClassName: OpenLog
 * Description: 開啟紀錄方法執行時間
 *
 * @Create 2024/9/4 下午11:26
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OpenLog {
}
