package com.shijiawei.secretblog.common.myenum;

import lombok.Getter;

/**
 * ClassName: RedisBloomFilterKey
 * Description:
 *
 * @Create 2025/10/9 下午7:57
 */
@Getter
public enum RedisBloomFilterKey {

    //文章ID布隆過濾器
    ARTICLE_BLOOM_FILTER("ams:bloom_filter:article"),
    COMMENT_BLOOM_FILTER("ams:bloom_filter:comment"),

    READY_BLOOM_ARTICLE("ready:bloom:article"),
    READY_BLOOM_COMMENT("ready:bloom:comment"),

    BLOOM_INIT_ARTICLE_LOCK("lock:bloom:init:article"),

    BLOOM_INIT_COMMENT_LOCK("lock:bloom:init:comment");
    private final String key;

    RedisBloomFilterKey(String key){
        this.key = key;
    }

}
