package com.shijiawei.secretblog.gateway.authentication.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.Map;
import java.util.Set;

/**
 * 權限檢查工具類
 * 提供路由和角色的匹配邏輯
 */
@Component
public class AuthorizationHelper {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationHelper.class);
    
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    
    private final Map<String, Set<String>> routeRoleMapping;
    private final Map<String, Map<HttpMethod, Set<String>>> methodSpecificRouteRoleMapping;
    private final Set<String> publicRoutes;
    private final Set<String> authenticatedRoutes;

    public AuthorizationHelper(Map<String, Set<String>> routeRoleMapping,
                             Map<String, Map<HttpMethod, Set<String>>> methodSpecificRouteRoleMapping,
                             Set<String> publicRoutes,
                             Set<String> authenticatedRoutes) {
        this.routeRoleMapping = routeRoleMapping;
        this.methodSpecificRouteRoleMapping = methodSpecificRouteRoleMapping;
        this.publicRoutes = publicRoutes;
        this.authenticatedRoutes = authenticatedRoutes;
    }

    /**
     * 檢查路由是否為公開路由（不需要認證）
     */
    public boolean isPublicRoute(String path) {
        return publicRoutes.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 檢查路由是否只需要認證（不限制角色）
     */
    public boolean isAuthenticatedRoute(String path) {
        return authenticatedRoutes.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 檢查用戶角色是否有權限訪問特定路由
     */
    public boolean hasRouteAccess(String path, HttpMethod method, String userRole) {
        if (userRole == null) {
            logger.debug("User role is null, access denied for path: {}", path);
            return false;
        }

        // 1. 檢查方法特定的權限配置
        Set<String> allowedRoles = getMethodSpecificAllowedRoles(path, method);
        if (allowedRoles != null) {
            boolean hasAccess = allowedRoles.contains(userRole);
            logger.debug("Method-specific authorization for {} {}: user role '{}', allowed roles {}, access: {}", 
                    method, path, userRole, allowedRoles, hasAccess);
            return hasAccess;
        }

        // 2. 檢查一般路由權限配置
        allowedRoles = getGeneralAllowedRoles(path);
        if (allowedRoles != null) {
            boolean hasAccess = allowedRoles.contains(userRole);
            logger.debug("General authorization for {}: user role '{}', allowed roles {}, access: {}", 
                    path, userRole, allowedRoles, hasAccess);
            return hasAccess;
        }

        // 3. 如果沒有明確配置，默認允許所有認證用戶
        logger.debug("No specific authorization rule for {}, allowing authenticated user with role: {}", path, userRole);
        return true;
    }

    /**
     * 獲取方法特定的允許角色
     */
    private Set<String> getMethodSpecificAllowedRoles(String path, HttpMethod method) {
        return methodSpecificRouteRoleMapping.entrySet().stream()
                .filter(entry -> pathMatcher.match(entry.getKey(), path))
                .map(entry -> entry.getValue().get(method))
                .findFirst()
                .orElse(null);
    }

    /**
     * 獲取一般路由的允許角色
     */
    private Set<String> getGeneralAllowedRoles(String path) {
        return routeRoleMapping.entrySet().stream()
                .filter(entry -> pathMatcher.match(entry.getKey(), path))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    /**
     * 獲取路由需要的角色列表（用於錯誤提示）
     */
    public Set<String> getRequiredRoles(String path, HttpMethod method) {
        Set<String> roles = getMethodSpecificAllowedRoles(path, method);
        if (roles != null) {
            return roles;
        }
        return getGeneralAllowedRoles(path);
    }
}