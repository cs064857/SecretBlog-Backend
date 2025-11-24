package com.shijiawei.secretblog.gateway.authentication.handler.login.business;

import com.shijiawei.secretblog.gateway.authentication.handler.login.UserLoginInfo;
import com.shijiawei.secretblog.gateway.config.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * 增強型 JWT 認證過濾器
 * 支持多種 Token 來源：Authorization Header 和 Cookie
 * 提供用戶資訊標頭傳遞和角色權限控制
 */
@Slf4j
@Component
public class EnhancedJwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedJwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public EnhancedJwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logger.debug("Enhanced JWT Authentication Filter processing request");

        ServerHttpRequest request = exchange.getRequest();
        String jwtToken = extractJwtToken(request);

        if (!StringUtils.hasText(jwtToken)) {
            logger.debug("No JWT token found in request");
            return chain.filter(exchange);
        }

        try {
            // 驗證並解析 JWT Token
            UserLoginInfo userLoginInfo = jwtService.verifyJwt(jwtToken, UserLoginInfo.class);

            if (userLoginInfo == null) {
                logger.debug("JWT token verification failed - null user info");
                return chain.filter(exchange);
            }

            logger.debug("JWT token verified successfully for user: {}, role: {}",
                    userLoginInfo.getUserId(), userLoginInfo.getRoleId());

            // 創建認證對象，包含角色資訊
            List<SimpleGrantedAuthority> authorities = createAuthorities(userLoginInfo.getRoleId());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userLoginInfo, null, authorities);

            // 添加用戶資訊到請求標頭
            ServerHttpRequest mutatedRequest = addUserHeadersToRequest(request, userLoginInfo);
            ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

            // 設置安全上下文並繼續過濾鏈
            return chain.filter(mutatedExchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (ExpiredJwtException e) {
            logger.debug("JWT token expired: {}", e.getMessage());
            // 過期的 Token 讓請求繼續，但不設置認證上下文，讓後續權限檢查處理
            return chain.filter(exchange);
        } catch (Exception e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            // 無效的 Token 讓請求繼續，但不設置認證上下文，讓後續權限檢查處理
            return chain.filter(exchange);
        }
    }

    /**
     * 從請求中提取 JWT Token
     * 優先級：Authorization Header > Cookie
     */
    private String extractJwtToken(ServerHttpRequest request) {
        // 1. 嘗試從 Authorization Header 獲取
        String authorizationHeader = request.getHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            logger.debug("JWT token extracted from Authorization header");
            return token;
        }

        // 2. 嘗試從 Cookie 獲取
        HttpCookie cookie = request.getCookies().getFirst("jwtToken");
        if (cookie != null && StringUtils.hasText(cookie.getValue())) {
            logger.debug("JWT token extracted from Cookie");
            return cookie.getValue();
        }

        return null;
    }

    /**
     * 根據角色 ID 創建 Spring Security 權限列表
     */
    private List<SimpleGrantedAuthority> createAuthorities(String roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }

        // 根據 Role 枚舉映射權限
        switch (roleId) {
            case "ADMIN":  // ADMIN
                return List.of(
                        new SimpleGrantedAuthority("ROLE_ADMIN"),
                        new SimpleGrantedAuthority("ROLE_USER")
                );
            case "NORMALUSER":  // NORMALUSER
                return List.of(new SimpleGrantedAuthority("ROLE_USER"));
            default:
                logger.warn("Unknown role ID: {}", roleId);
                return Collections.emptyList();
        }
    }

    /**
     * 添加用戶資訊到請求標頭，供下游微服務使用
     */
    private ServerHttpRequest addUserHeadersToRequest(ServerHttpRequest request, UserLoginInfo userInfo) {
        log.info("添加用戶標頭:{}", userInfo);
        return request.mutate()
                .header("X-User-Id", String.valueOf(userInfo.getUserId()))
                .header("X-User-Role", mapRoleIdToName(userInfo.getRoleId()))
                .header("X-Session-Id", userInfo.getSessionId())
                .header("X-User-Nickname", userInfo.getNickname() != null ? userInfo.getNickname() : "")
                .header("X-Token-Exp", String.valueOf(userInfo.getExpiredTime()))
                .build();
    }

    /**
     * 將角色 ID 轉換為可讀的角色名稱
     * 支援多種來源格式：數字("0"/"1") 或 枚舉字串("ADMIN"/"NORMALUSER"/"USER")
     */
    private String mapRoleIdToName(String roleId) {
        if (!StringUtils.hasText(roleId)) {
            return "UNKNOWN";
        }
        switch (roleId) {
            // 數字ID對應
//            case "0":
//                return "ADMIN";
//            case "1":
//                return "USER";
            // 枚舉字串對應
            case "ADMIN":
                return "ADMIN";
            case "NORMALUSER":
            case "USER":
                return "USER";
            default:
                return "UNKNOWN";
        }
    }

}