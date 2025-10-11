package com.shijiawei.secretblog.common.myenum;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ClassName: RedisLockKey
 * Description:
 *
 * @Create 2025/10/11 下午11:22
 */
@AllArgsConstructor
public enum RedisLockKey {
    BLOOM_INIT_ARTICLE_LOCK("lock:bloom:init:article"),
    BLOOM_INIT_COMMENT_LOCK("lock:bloom:init:comment");



    @Getter
    private final String Key;
}
