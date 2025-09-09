package com.shijiawei.secretblog.common.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用戶上下文持有者
 * 用於從網關傳遞的請求標頭中提取用戶資訊
 */
public class UserContextHolder {

    // 用戶資訊標頭常量
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_ROLE = "X-User-Role";
    private static final String HEADER_SESSION_ID = "X-Session-Id";
    private static final String HEADER_USER_NICKNAME = "X-User-Nickname";
    private static final String HEADER_TOKEN_EXP = "X-Token-Exp";

    /**
     * 獲取當前用戶 ID
     */
    public static Long getCurrentUserId() {
        String userIdStr = getHeader(HEADER_USER_ID);
        try {
            return userIdStr != null ? Long.valueOf(userIdStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 獲取當前用戶角色
     */
    public static String getCurrentUserRole() {
        return getHeader(HEADER_USER_ROLE);
    }

    /**
     * 獲取當前會話 ID
     */
    public static String getCurrentSessionId() {
        return getHeader(HEADER_SESSION_ID);
    }

    /**
     * 獲取當前用戶暱稱
     */
    public static String getCurrentUserNickname() {
        return getHeader(HEADER_USER_NICKNAME);
    }

    /**
     * 獲取 Token 過期時間
     */
    public static Long getTokenExpiredTime() {
        String expStr = getHeader(HEADER_TOKEN_EXP);
        try {
            return expStr != null ? Long.valueOf(expStr) : null;
        } catch (NumberFormatException e) {
            return null;
        }
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

    /**
     * 獲取當前用戶的完整資訊
     */
    public static UserInfo getCurrentUserInfo() {
        return UserInfo.builder()
                .userId(getCurrentUserId())
                .role(getCurrentUserRole())
                .sessionId(getCurrentSessionId())
                .nickname(getCurrentUserNickname())
                .tokenExpiredTime(getTokenExpiredTime())
                .build();
    }

    /**
     * 從請求標頭中獲取指定的值
     */
    private static String getHeader(String headerName) {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader(headerName);
        }
        return null;
    }

    /**
     * 用戶資訊封裝類
     */
    public static class UserInfo {
        private Long userId;
        private String role;
        private String sessionId;
        private String nickname;
        private Long tokenExpiredTime;

        public UserInfo() {}

        public UserInfo(Long userId, String role, String sessionId, String nickname, Long tokenExpiredTime) {
            this.userId = userId;
            this.role = role;
            this.sessionId = sessionId;
            this.nickname = nickname;
            this.tokenExpiredTime = tokenExpiredTime;
        }

        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }

        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }

        public Long getTokenExpiredTime() { return tokenExpiredTime; }
        public void setTokenExpiredTime(Long tokenExpiredTime) { this.tokenExpiredTime = tokenExpiredTime; }

        @Override
        public String toString() {
            return "UserInfo{" +
                    "userId=" + userId +
                    ", role='" + role + '\'' +
                    ", sessionId='" + sessionId + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", tokenExpiredTime=" + tokenExpiredTime +
                    '}';
        }

        public static class UserInfoBuilder {
            private Long userId;
            private String role;
            private String sessionId;
            private String nickname;
            private Long tokenExpiredTime;

            public UserInfoBuilder userId(Long userId) {
                this.userId = userId;
                return this;
            }

            public UserInfoBuilder role(String role) {
                this.role = role;
                return this;
            }

            public UserInfoBuilder sessionId(String sessionId) {
                this.sessionId = sessionId;
                return this;
            }

            public UserInfoBuilder nickname(String nickname) {
                this.nickname = nickname;
                return this;
            }

            public UserInfoBuilder tokenExpiredTime(Long tokenExpiredTime) {
                this.tokenExpiredTime = tokenExpiredTime;
                return this;
            }

            public UserInfo build() {
                return new UserInfo(userId, role, sessionId, nickname, tokenExpiredTime);
            }
        }
    }
}