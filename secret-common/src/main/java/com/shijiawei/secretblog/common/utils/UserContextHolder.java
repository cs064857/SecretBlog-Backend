package com.shijiawei.secretblog.common.utils;

import com.shijiawei.secretblog.common.security.JwtUserInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 用戶上下文持有者
 * 用於從 Spring Security 的 SecurityContext 中提取用戶資訊
 */
public class UserContextHolder {

    /**
     * 獲取當前用戶 ID
     */
    public static Long getCurrentUserId() {
        JwtUserInfo userInfo = getCurrentUserInfoFromSecurityContext();
        return userInfo != null ? userInfo.getUserId() : null;
    }

    /**
     * 獲取當前用戶角色///TODO !帶修正
     */
    public static String getCurrentUserRole() {
        JwtUserInfo userInfo = getCurrentUserInfoFromSecurityContext();
        return userInfo != null ? userInfo.getRoleId().getCode() : null;
    }

    /**
     * 獲取當前會話 ID
     */
    public static String getCurrentSessionId() {
        JwtUserInfo userInfo = getCurrentUserInfoFromSecurityContext();
        return userInfo != null ? userInfo.getSessionId() : null;
    }

    /**
     * 獲取當前用戶暱稱
     */
    public static String getCurrentUserNickname() {
        JwtUserInfo userInfo = getCurrentUserInfoFromSecurityContext();
        return userInfo != null ? userInfo.getNickname() : null;
    }

    /**
     * 獲取當前用戶暱稱
     */
    public static String getCurrentAvatar() {
        JwtUserInfo userInfo = getCurrentUserInfoFromSecurityContext();
        return userInfo != null ? userInfo.getAvatar() : null;
    }

    /**
     * 獲取 Token 過期時間
     */
    public static Long getTokenExpiredTime() {
        JwtUserInfo userInfo = getCurrentUserInfoFromSecurityContext();
        return userInfo != null ? userInfo.getExpiredTime() : null;
    }

    /**
     * 檢查當前用戶是否為管理員
     */
    public static boolean isCurrentUserAdmin() {
        return "ADMIN".equals(getCurrentUserRole());
    }

    /**
     * 檢查當前用戶是否已登入
     */
    public static boolean isCurrentUserLoggedIn() {
        return getCurrentUserId() != null;
    }

    private static JwtUserInfo getCurrentUserInfoFromSecurityContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof JwtUserInfo) {
            return (JwtUserInfo) principal;
        }
        // 相容：若 principal 不是 JwtUserInfo，嘗試轉型（例如被包成 Map）
        try {
            return JSON.convert(principal, JwtUserInfo.class);
        } catch (Exception ignored) {
            return null;
        }
    }
}
