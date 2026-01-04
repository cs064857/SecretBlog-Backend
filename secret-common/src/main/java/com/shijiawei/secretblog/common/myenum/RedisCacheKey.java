package com.shijiawei.secretblog.common.myenum;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
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
    //已使用
    ARTICLE_LIKES("ams:article:likes:%s", "文章點讚數（計數用）", null),
    //已使用
    ARTICLE_VIEWS("ams:article:views:%s", "文章瀏覽數（計數用）", null),
    //已使用
    ARTICLE_COMMENTS("ams:article:comments:%s", "文章留言數（計數用）", null),
    //已使用
    ARTICLE_BOOKMARKS("ams:article:bookmarks:%s", "文章書籤數（計數用）", null),

    ARTICLE_STATUS("ams:article:status:%s","文章指標", Duration.ofMinutes(30)),
    ARTICLE_TAGS("ams:article:tags","文章所有標籤",null),




    //紀錄用戶是否以及點讚過該文章 已使用
    ARTICLE_LIKED_USERS("ams:article:%s:liked", "文章點讚用戶集合", Duration.ofMinutes(30)),
    //紀錄用戶是否以及書籤過該文章 已使用
    ARTICLE_MARKED_USERS("ams:article:%s:marked", "文章書籤用戶集合", Duration.ofMinutes(30)),

    /**
     * 用於記錄用戶點讚、瀏覽、收藏的文章ID集合，方便快速查詢用戶行為
     */
    USER_LIKED_ARTICLES("ams:user:%s:liked:articles", "用戶點讚文章集合", null),
    USER_VIEWED_ARTICLES("ams:user:%s:viewed:articles", "用戶瀏覽文章集合", null),
    USER_BOOKMARKED_ARTICLES("ams:user:%s:bookmarked:articles", "用戶收藏文章集合", null),

    /**
     * 留言計數
     */
//    ARTICLE_COMMENTS_LIKES("ams:article:comment:%s:likes_count", "文章留言點讚數（計數用）", null),
    ARTICLE_COMMENTS_BOOKMARKS("ams:article:comment:%s:bookmarks_count", "文章留言書籤數（計數用）", null),

    /**
     * 留言相關
     */

    // 文章的留言列表索引（ZSet，按時間排序）
//    ARTICLE_COMMENT_IDS("ams:article:comments:%s:comment_ids", "文章留言ID集合(ZSet)", null),

    // 文章留言的點讚數聚合（Hash: field=commentId, value=likesCount）已使用
    ARTICLE_COMMENT_LIKES_COUNT_HASH("ams:article:comment:%s:comment_likes", "文章留言點讚數Hash", Duration.ofMinutes(30)),
    // 文章留言的留言數聚合（Hash: field=commentId, value=replies_count）已使用
    ARTICLE_COMMENT_REPLIES_COUNT_HASH("ams:article:comment:%s:replies_count", "文章留言的回覆數Hash", Duration.ofMinutes(30)),

    // 文章留言的書籤數聚合（Hash: field=commentId, value=bookmarksCount）
    ARTICLE_COMMENT_BOOKMARKS_HASH("ams:article:comment:%s:comment_bookmarks", "文章留言書籤數Hash", null),

    // 留言被哪些用戶點讚（Set: 用戶ID集合）已使用
    COMMENT_LIKED_USERS("ams:comment:%s:liked_users", "留言點讚用戶集合", null),

    // 留言被哪些用戶書籤（Set: 用戶ID集合）
    COMMENT_MARKED_USERS("ams:comment:%s:marked_users", "留言書籤用戶集合", null),

    /**
     * 使用者 / 認證相關
     */
    JWT_BLACKLIST_SESSION("blacklist:jwt:%s", "JWT 黑名單（以 sessionId 作為鍵）", null),

    USER_EMAIL_VALID_CODE("umsuser:validcode_%s", "用戶註冊信箱驗證碼", Duration.ofMinutes(15)),
    USER_EMAIL_VALID_CODE_RATE_LIMIT_IP("umsuser:validcode_ratelimit_ipaddr_%s", "信箱驗證碼寄送限流（依 IP）", null),

    USER_FORGOT_PASSWORD_RATE_LIMIT_IP("umsuser:forgot_password_ratelimit_ipaddr_%s", "忘記密碼限流（依 IP）", null),
    USER_PASSWORD_RESET_TOKEN("umsuser:password_reset_token_%s", "密碼重設 Token -> UserId 映射", Duration.ofMinutes(30)),

    /**
     * ===== 快取維護 / 基礎設施 =====
     */
    CACHE_DELETION_QUEUE("cacheDeletionQueue:%s", "延遲雙刪佇列（依 keyPrefix 分流）", null);




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
//            throw new CustomRuntimeException(ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING.getCode(), ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING.getMessage());
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REDIS_KEY_FORMAT_PARAM_MISSING)
                    .build();

        }
        return String.format(pattern, args);
    }

    public String getPattern(){
        return this.pattern;
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
