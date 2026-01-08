package com.shijiawei.secretblog.common.myenum;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * ClassName: RedisLockKey
 * Description:
 *
 * @Create 2025/10/11 下午11:22
 */
@AllArgsConstructor
public enum RedisLockKey {


    ARTICLE_EXISTS_LOCK("lock:article:exists:%s"),
    ARTICLE_COMMENTS_EXISTS_LOCK("lock:article:comments:exists:%s"),
    ARTICLE_STATUS_LOCK("lock:article:status:%s"),
    ARTICLE_TAGS_LOCK("lock:article:tags"),
    ARTICLE_COMMENTS_LIKES_LOCK("lock:article:comments:likes:%s"),

    //OpenCacheAspect 使用的鎖鍵：以快取 key 為基底加上"_Lock""後綴。
    OPEN_CACHE_LOCK("%s_Lock");

    @Getter
    private final String pattern;

    public String getFormat(Object... args){
        if(Arrays.stream(args).anyMatch(Objects::isNull)){
//            throw new CustomRuntimeException(ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING.getCode(),ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING.getMessage());
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING)
                    .build();
        }


        try {
            return String.format(Locale.ROOT, pattern, args);
        } catch (IllegalFormatException e) {
            //假設捕獲到格式化異常，則拋出異常
//            throw new CustomRuntimeException(ResultCode.REDIS_KEY_FORMAT_ERROR.getCode(),ResultCode.PARAM_ERROR.getMessage(),ResultCode.REDIS_KEY_FORMAT_ERROR.getMessage(), e.getCause());

            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REDIS_KEY_FORMAT_ERROR)
                    .cause(e.getCause())
                    .build();

        }


    }
}
