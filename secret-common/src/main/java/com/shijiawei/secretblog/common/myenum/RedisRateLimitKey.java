package com.shijiawei.secretblog.common.myenum;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;

import java.util.Arrays;
import java.util.Objects;

/**
 * ClassName: RedisRateLimitKey
 * Description:
 *
 * @Create 2025/10/8 上午1:31
 */
@AllArgsConstructor

public enum RedisRateLimitKey {

    /**
     * 限流
     */

    // 全局級別限流（按業務區分）
    RATE_LIMIT_GLOBAL_LIKE("ams:rate_limit:like:global", "點讚全局限流", RateType.OVERALL, 20000, 1, RateIntervalUnit.MINUTES),
    RATE_LIMIT_GLOBAL_COMMENT("ams:rate_limit:comment:global", "留言全局限流", RateType.OVERALL,20000, 1, RateIntervalUnit.MINUTES),

    // 用戶級別限流（按業務區分）
    RATE_LIMIT_USER_LIKE("ams:rate_limit:like:user:%s", "用戶點讚限流", RateType.OVERALL,30, 5, RateIntervalUnit.MINUTES),
    RATE_LIMIT_USER_COMMENT("ams:rate_limit:comment:user:%s", "用戶留言限流", RateType.OVERALL,10, 5, RateIntervalUnit.MINUTES),
    RATE_LIMIT_USER_POST("ams:rate_limit:post:user:%s", "用戶發文限流", RateType.OVERALL,5, 60, RateIntervalUnit.MINUTES),

    // IP 級別限流（按業務區分）
    RATE_LIMIT_IP_LIKE("ams:rate_limit:like:ip:%s", "IP 點讚限流", RateType.OVERALL,30, 5, RateIntervalUnit.MINUTES),
    RATE_LIMIT_IP_REGISTER("ams:rate_limit:register:ip:%s", "IP 註冊限流", RateType.OVERALL,5, 60, RateIntervalUnit.MINUTES);


    private final String pattern;
    @Getter
    private final String description;
    @Getter
    private final RateType rateType;
    @Getter
    private final long Rate;
    @Getter
    private final long rateInterval;
    @Getter
    private final RateIntervalUnit rateIntervalUnit;

    public String format(Object... args){
        // null 檢查 / 長度檢查
        if(Arrays.stream(args).anyMatch(Objects::isNull)){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING)
                    .build();
        }

        return String.format(pattern,args);

    }

}
