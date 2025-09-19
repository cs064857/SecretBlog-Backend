//package com.shijiawei.secretblog.gateway.authentication.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.Set;
//
///**
// * 路由權限控制配置
// * 定義不同路由的角色訪問權限
// */
//@Configuration
//public class RouteAuthorizationConfig {
//
//    /**
//     * 路由權限映射配置
//     * Key: 路徑模式, Value: 允許的角色集合
//     */
//    @Bean
//    public Map<String, Set<String>> routeRoleMapping() {
//        Map<String, Set<String>> routeRoles = new HashMap<>();
//
//        // 管理員專屬路由 - 只有 ADMIN 可以訪問
//        routeRoles.put("/api/admin/**", Set.of("ADMIN"));
//
//        // 用戶管理相關 - 管理員專屬
//        routeRoles.put("/api/ums/user/delete/**", Set.of("ADMIN"));
//        routeRoles.put("/api/ums/role/**", Set.of("ADMIN"));
//        routeRoles.put("/api/ums/status/**", Set.of("ADMIN"));
//
//        // 文章管理 - 登錄用戶都可以訪問
//        routeRoles.put("/api/article/**", Set.of("USER", "ADMIN"));
//        // 評論點讚 - 登錄用戶都可以訪問
//        routeRoles.put("/api/article/comment/**", Set.of("USER", "ADMIN"));
//
//        // 用戶個人資料 - 登錄用戶都可以訪問
//        routeRoles.put("/api/ums/user/profile/**", Set.of("USER", "ADMIN"));
//        routeRoles.put("/api/ums/user/update/**", Set.of("USER", "ADMIN"));
//
//        // 存儲服務 - 登錄用戶都可以訪問
//        routeRoles.put("/api/sms/**", Set.of("USER", "ADMIN"));
//
//        return routeRoles;
//    }
//
//    /**
//     * HTTP 方法特定的權限配置
//     * Key: 路徑模式, Value: Map<HttpMethod, 允許的角色集合>
//     */
//    @Bean
//    public Map<String, Map<HttpMethod, Set<String>>> methodSpecificRouteRoleMapping() {
//        Map<String, Map<HttpMethod, Set<String>>> methodRoles = new HashMap<>();
//
//        // 文章相關的方法級權限
//        Map<HttpMethod, Set<String>> articleMethods = new HashMap<>();
//        articleMethods.put(HttpMethod.GET, Set.of("USER", "ADMIN"));      // 查看文章
//        articleMethods.put(HttpMethod.POST, Set.of("USER", "ADMIN"));     // 創建文章
//        articleMethods.put(HttpMethod.PUT, Set.of("USER", "ADMIN"));      // 更新文章
//        articleMethods.put(HttpMethod.DELETE, Set.of("ADMIN"));           // 刪除文章 - 僅管理員
//
//        methodRoles.put("/api/article/**", articleMethods);
//
//        // 用戶資料相關的方法級權限
//        Map<HttpMethod, Set<String>> userMethods = new HashMap<>();
//        userMethods.put(HttpMethod.GET, Set.of("USER", "ADMIN"));         // 查看用戶資料
//        userMethods.put(HttpMethod.PUT, Set.of("USER", "ADMIN"));         // 更新用戶資料
//        userMethods.put(HttpMethod.DELETE, Set.of("ADMIN"));              // 刪除用戶 - 僅管理員
//
//        methodRoles.put("/api/ums/user/**", userMethods);
//
//        return methodRoles;
//    }
//
//    /**
//     * 公開路由配置 - 不需要任何認證
//     */
//    @Bean
//    public Set<String> publicRoutes() {
//        return Set.of(
//            "/api/ums/user/login/**",
//            "/api/ums/user/register",
//            "/api/ums/user/email-verify-code",
//            "/api/public/**",
//            "/actuator/**",
//            "/favicon.ico"
//        );
//    }
//
//    /**
//     * 認證路由配置 - 需要登錄但不限制角色
//     */
//    @Bean
//    public Set<String> authenticatedRoutes() {
//        return Set.of(
//            "/api/ums/user/profile",
//            "/api/ums/user/logout"
//        );
//    }
//}