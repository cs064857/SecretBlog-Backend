package com.shijiawei.secretblog.common.myenum;

import com.shijiawei.secretblog.common.exception.CustomBaseException;
import lombok.Getter;

import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;

/**
 * ClassName: RedisCacheKey
 * Description:
 *
 * @Create 2025/10/3 上午1:37
 */

public enum RedisCacheKey {



    /**
     * 文章計數
     */

    ARTICLE_LIKES("ams:article:likes:%s", "文章點讚數（計數用）", null),
    ARTICLE_VIEWS("ams:article:views:%s", "文章瀏覽數（計數用）", null),
    ARTICLE_COMMENTS("ams:article:comments:%s", "文章留言數（計數用）", null),
    ARTICLE_BOOKMARKS("ams:article:bookmarks:%s", "文章書籤數（計數用）", null),






    //紀錄用戶是否以及點讚過該文章
    ARTICLE_LIKED_USERS("ams:article:%s:liked", "文章點讚用戶集合", null),
    ARTICLE_MARKED_USERS("ams:article:%s:marked", "文章書籤用戶集合", null),

    /**
     * 用於記錄用戶點讚、瀏覽、收藏的文章ID集合，方便快速查詢用戶行為
     */
    USER_LIKED_ARTICLES("ams:user:%s:liked:articles", "用戶點讚文章集合", null),
    USER_VIEWED_ARTICLES("ams:user:%s:viewed:articles", "用戶瀏覽文章集合", null),
    USER_BOOKMARKED_ARTICLES("ams:user:%s:bookmarked:articles", "用戶收藏文章集合", null);

//    ARTICLE_USER_COMMENTED("ams:article:comments:user:%s:%s", "文章留言用戶記錄（防止重複計數用）", null),


//    ARTICLE_USER_VIEWED("ams:article:views:user:%s:%s", "文章瀏覽用戶記錄（防止重複計數用）", null),
//    ARTICLE_USER_COMMENTED("ams:article:comments:user:%s:%s", "文章留言用戶記錄（防止重複計數用）", null),
//    ARTICLE_USER_BOOKMARKS("ams:article:bookmarks:user:%s:%s", "文章書籤用戶記錄（防止重複計數用）", null);

    private final String pattern;
    @Getter
    private final String description;
    @Getter
    private final Duration ttl;

    RedisCacheKey(String pattern, String description, Duration ttl) {
        this.pattern = pattern;
        this.description = description;
        this.ttl = ttl;
    }

    public String format(Object... args) {
        // null 檢查 / 長度檢查
        if(Arrays.stream(args).anyMatch(Objects::isNull)){
            throw new CustomBaseException("RedisKey模板參數值不可為空");
        }
        return String.format(pattern, args);
    }



}



//public enum RedisCacheKey {
//
//    ARTICLE_VIEWS("arams:article:views", Duration.ofDays(30), "文章查看數緩存"),
//    ARTICLE_LIKES("ams:article:likes", Duration.ofDays(30), "文章點讚數緩存"),
//    ARTICLE_COMMENT("ams:article:comments", Duration.ofDays(30), "文章留言數緩存");
//
//
//    private final String prefix;
//    private final Duration ttl;
//    private final String description;
//
//    RedisCacheKey(String prefix, Duration ttl, String description) {
//        this.prefix = prefix;
//        this.ttl = ttl;
//        this.description = description;
//    }
//
//    public String buildKeyName(Object... args) {
//
//        // 1. 建立一個列表，用來存放 Key 的所有部分
//        List<String> parts = new ArrayList<>();
//        // 2. 加入乾淨的前綴
//        parts.add(this.prefix);
//        // 3. 將所有動態參數轉換為字串後加入列表
//        if (args != null && args.length > 0) {
//            Arrays.stream(args)
//                    .filter(Objects::nonNull)
//                    .map(String::valueOf)
//                    .forEach(parts::add);
//        }
//
//        // 5. 使用 String.join 將列表中的所有元素用冒號連接起來
//        return String.join(":", parts);
//    }
//}
