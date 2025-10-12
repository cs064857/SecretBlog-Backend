package com.shijiawei.secretblog.article.config;

import com.shijiawei.secretblog.common.myenum.RedisBloomFilterKey;
import jakarta.annotation.Resource;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: BloomFilterConfig
 * Description:
 *
 * @Create 2025/10/9 上午2:41
 */
@Configuration
public class BloomFilterConfig {

    @Resource
    private RedissonClient redissonClient;

    @Bean(name = "articleBloomFilter")
    public RBloomFilter<Long> articleBloomFilter(){
        RBloomFilter<Long> articleBloomFilter = redissonClient.getBloomFilter(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey());
        boolean tryInit = articleBloomFilter.tryInit(1_000_000L, 0.01);
        return articleBloomFilter;
    }

    @Bean(name = "commentBloomFilter")
    public RBloomFilter<Long> commentBloomFilter(){
        RBloomFilter<Long> commentBloomFilter = redissonClient.getBloomFilter(RedisBloomFilterKey.COMMENT_BLOOM_FILTER.getKey());
        boolean tryInit = commentBloomFilter.tryInit(1_000_000L, 0.01);
        return commentBloomFilter;
    }


}
