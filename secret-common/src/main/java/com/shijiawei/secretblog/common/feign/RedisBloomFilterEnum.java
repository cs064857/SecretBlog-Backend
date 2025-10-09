package com.shijiawei.secretblog.common.feign;

import lombok.Getter;

/**
 * ClassName: RedisBloomFilterEnum
 * Description:
 *
 * @Create 2025/10/9 下午7:57
 */
@Getter
public enum RedisBloomFilterEnum {

    //文章ID布隆過濾器
    ARTICLE_BLOOM_FILTER("ams:bloom_filter:article");


    private final String pattern;

    RedisBloomFilterEnum(String pattern){
        this.pattern=pattern;
    }

}
