package com.shijiawei.secretblog.common.security;

/**
 * JWT 黑名單服務介面（用於登出失效等情境）。
 *
 */
public interface JwtBlacklistService {

    /**
     * 將指定 sessionId 加入黑名單（通常以 Token 剩餘有效期作為 TTL）。
     */
    void blacklist(String sessionId, long ttlMillis);

    /**
     * 檢查指定 sessionId 是否已在黑名單中。
     */
    boolean isBlacklisted(String sessionId);
}

