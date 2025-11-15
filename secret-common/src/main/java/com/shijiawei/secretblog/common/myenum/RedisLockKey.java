package com.shijiawei.secretblog.common.myenum;

import com.shijiawei.secretblog.common.exception.CustomBaseException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.Objects;

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
    ARTICLE_COMMENTS_LIKES_LOCK("lock:article:comments:likes:%s");

    @Getter
    private final String pattern;

    public String getFormat(Object... args){
        if(Arrays.stream(args).anyMatch(Objects::isNull)){
            throw new CustomBaseException("RedisKey模板參數值不可為空");
        }


        try {
            return String.format(Locale.ROOT, pattern, args);
        } catch (IllegalFormatException e) {
            throw new CustomBaseException("鍵格式與參數不匹配", e);
        }


    }
}
