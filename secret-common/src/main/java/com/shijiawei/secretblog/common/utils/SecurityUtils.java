package com.shijiawei.secretblog.common.utils;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import org.apache.http.protocol.HTTP;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Spring Security 工具類
 * 用於獲取當前登錄用戶信息
 */
public class SecurityUtils {

    /**
     * 獲取當前登錄用戶的完整信息
     *
     * @return UserLoginInfo 當前用戶信息
     * @throws RuntimeException 如果用戶未登錄或認證信息無效
     */
    public static <T> T getCurrentUser(Class<T> userClass) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();

            if (userClass.isInstance(principal)) {
                return userClass.cast(principal);
            }
        }

//        throw new CustomRuntimeException(HttpStatus.FORBIDDEN.getReasonPhrase(),HttpStatus.FORBIDDEN,HttpStatus.FORBIDDEN.getReasonPhrase());
        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.FORBIDDEN)
                .build();
    }

    /**
     * 獲取當前登錄用戶的 ID
     * 注意：這個方法需要 UserLoginInfo 類有 getUserId() 方法
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                // 使用反射獲取 userId，避免直接依賴 UserLoginInfo 類
                if (principal != null) {
                    try {
                        java.lang.reflect.Method getUserIdMethod = principal.getClass().getMethod("getUserId");
                        Object userId = getUserIdMethod.invoke(principal);

                        if (userId instanceof Long) {
                            return (Long) userId;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("無法獲取用戶ID: " + e.getMessage());
                    }
                }
            }

            throw new RuntimeException("用戶未登錄");
        } catch (Exception e) {
            throw new RuntimeException("獲取當前用戶ID失敗: " + e.getMessage());
        }
    }

    /**
     * 獲取當前登錄用戶的昵稱
     */
    public static String getCurrentUserNickname() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()) {
                Object principal = authentication.getPrincipal();

                if (principal != null) {
                    try {
                        java.lang.reflect.Method getNicknameMethod = principal.getClass().getMethod("getNickname");
                        Object nickname = getNicknameMethod.invoke(principal);

                        if (nickname instanceof String) {
                            return (String) nickname;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException("無法獲取用戶昵稱: " + e.getMessage());
                    }
                }
            }

            throw new RuntimeException("用戶未登錄");
        } catch (Exception e) {
            throw new RuntimeException("獲取當前用戶昵稱失敗: " + e.getMessage());
        }
    }

    /**
     * 檢查用戶是否已登錄
     */
    public static boolean isAuthenticated() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return authentication != null &&
                    authentication.isAuthenticated() &&
                    !"anonymousUser".equals(authentication.getPrincipal());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 獲取當前認證對象
     */
    public static Authentication getCurrentAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 清除當前認證信息
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}