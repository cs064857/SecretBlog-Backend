package com.shijiawei.secretblog.common.redisutils;

/**
 * ClassName: RedisLuaScripts
 * Description:
 *
 * @Create 2025/10/31 上午12:09
 */
public class RedisLuaScripts {

    /**
     * 留言點讚 Lua 腳本
     * KEYS[1] = ams:comment:{commentId}:liked_users (用戶集合)
     * KEYS[2] = ams:article:{articleId}:comment_likes (Hash)
     * ARGV[1] = userId
     * ARGV[2] = commentId
     *
     * 用戶點讚
     *    ↓
     * 嘗試將 userId 加入集合
     *    ↓
     * 成功？ → 是 → 點讚數 +1 → 返回 1
     *    ↓
     *    否 → 已點過讚 → 返回 0
     *
     */
    public static final String LIKE_COMMENT_SCRIPT =
                    "local added = redis.call('SADD', KEYS[1], ARGV[1]) " + // 嘗試將userId加入集合
                    "if added == 1 then " + //判斷是否成功加入集合,假設userId不存在於KEYS[1]鍵中，代表沒有點讚過該留言，才能成功加入集合;否則無法加入集合(已經對該留言點過讚)
                    "  local newCount = redis.call('HINCRBY', KEYS[2], ARGV[2], 1) " + // 更新某個文章(KEYS[2])鍵中某個留言(ARGV[2])字段的按讚數加1
                    "  return newCount " + //增加按讚數成功, 返回新的按讚數
                    "else " +
                    "  return -1 " + //增加按讚數失敗, 返回-1
                    "end";

    /**
     * 取消留言點讚 Lua 腳本
     *
     * 用戶取消點讚
     *    ↓
     * 嘗試從集合移除 userId
     *    ↓
     * 成功？ → 是 → 點讚數 -1 → 返回 1
     *    ↓
     *    否 → 沒點過讚 → 返回 0
     *
     */
    public static final String UNLIKE_COMMENT_SCRIPT =
                    "local removed = redis.call('SREM', KEYS[1], ARGV[1]) " +
                    "if removed == 1 then " +
                    "  local newCount =redis.call('HINCRBY', KEYS[2], ARGV[2], -1) " +
                    "  return newCount " +
                    "else " +
                    "  return -1 " + //取消按讚數失敗, 可能是本來就沒有點讚過該留言, 或者是已經對該留言點過讚, 返回-1
                    "end";

    /**
     * 判斷值是否在集合中 Lua 腳本
     * KEYS[1] = 集合鍵名
     * ARGV[1] = 要檢查的值
     *
     * 返回值：
     * 1 = 值存在於集合中
     * 0 = 值不存在於集合中
     */
    public static final String CHECK_VALUE_IN_SET_SCRIPT =
            "local contains = redis.call('SISMEMBER',KEYS[1], ARGV[1]) \n" +
                    "return contains";

    /**
     * 刪除留言相關的 Redis 數據 Lua 腳本
     * KEYS[1] = ams:comment:{commentId}:liked_users (點讚用戶集合)
     * KEYS[2] = ams:comment:{commentId}:marked_users (收藏用戶集合)
     * KEYS[3] = ams:article:{articleId}:comment_likes (點讚數 Hash)
     * KEYS[4] = ams:article:{articleId}:comment_replies (回覆數 Hash)
     * ARGV[1] = commentId
     *
     * 刪除留言
     *    ↓
     * 刪除點讚用戶集合
     *    ↓
     * 刪除收藏用戶集合
     *    ↓
     * 從點讚數 Hash 移除該留言
     *    ↓
     * 從回覆數 Hash 移除該留言
     *    ↓
     * 返回 1 (成功)
     */
    public static final String DELETE_COMMENT_SCRIPT =
            "redis.call('DEL', KEYS[1]) " + // 刪除點讚用戶集合
            "redis.call('DEL', KEYS[2]) " + // 刪除收藏用戶集合
            "redis.call('HDEL', KEYS[3], ARGV[1]) " + // 從點讚數 Hash 移除
            "redis.call('HDEL', KEYS[4], ARGV[1]) " + // 從回覆數 Hash 移除
            "return 1";

    /**
     * 刪除文章相關的 Redis 數據 Lua 腳本
     * KEYS[1] = ams:article:likes:{articleId} (文章點讚數)
     * KEYS[2] = ams:article:views:{articleId} (文章瀏覽數)
     * KEYS[3] = ams:article:comments:{articleId} (文章留言數)
     * KEYS[4] = ams:article:bookmarks:{articleId} (文章書籤數)
     * KEYS[5] = ams:article:status:{articleId} (文章指標 Hash)
     * KEYS[6] = ams:article:{articleId}:liked (文章點讚用戶集合)
     * KEYS[7] = ams:article:{articleId}:marked (文章書籤用戶集合)
     *
     * 刪除文章
     *    ↓
     * 刪除文章點讚數
     *    ↓
     * 刪除文章瀏覽數
     *    ↓
     * 刪除文章留言數
     *    ↓
     * 刪除文章書籤數
     *    ↓
     * 刪除文章指標 Hash
     *    ↓
     * 刪除文章點讚用戶集合
     *    ↓
     * 刪除文章書籤用戶集合
     *    ↓
     * 返回 1 (成功)
     */
    public static final String DELETE_ARTICLE_SCRIPT =
            "redis.call('DEL', KEYS[1]) " + // 刪除文章點讚數
            "redis.call('DEL', KEYS[2]) " + // 刪除文章瀏覽數
            "redis.call('DEL', KEYS[3]) " + // 刪除文章留言數
            "redis.call('DEL', KEYS[4]) " + // 刪除文章書籤數
            "redis.call('DEL', KEYS[5]) " + // 刪除文章指標 Hash
            "redis.call('DEL', KEYS[6]) " + // 刪除文章點讚用戶集合
            "redis.call('DEL', KEYS[7]) " + // 刪除文章書籤用戶集合
            "return 1";
}


