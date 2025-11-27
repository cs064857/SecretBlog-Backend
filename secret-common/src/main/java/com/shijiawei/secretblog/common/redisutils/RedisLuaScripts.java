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
}
