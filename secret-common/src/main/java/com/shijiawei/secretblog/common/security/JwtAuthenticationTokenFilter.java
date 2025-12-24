package com.shijiawei.secretblog.common.security;

import com.shijiawei.secretblog.common.enumValue.Role;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * 共用 JWT 驗證 Filter
 * 從 Cookie 中提取 Token
 * 使用 JwtService 驗簽與驗過期
 * 將從jwtToken中解析出的用戶信息建立並放入SecurityContextHolder上下文中
 */
@Slf4j
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {


    private final JwtService jwtService;

    @Nullable
    private final JwtBlacklistService jwtBlacklistService;

    public JwtAuthenticationTokenFilter(JwtService jwtService) {
        this(jwtService, null);
    }

    public JwtAuthenticationTokenFilter(JwtService jwtService, @Nullable JwtBlacklistService jwtBlacklistService) {
        this.jwtService = jwtService;
        this.jwtBlacklistService = jwtBlacklistService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            JwtUserInfo userInfo = jwtService.verifyJwt(token, JwtUserInfo.class);
            if (userInfo == null || userInfo.getUserId() == null) {
                filterChain.doFilter(request, response);
                return;
            }

            // 登出黑名單，若 sessionId 已被註銷，則不建立認證上下文。
            if (jwtBlacklistService != null
                    && StringUtils.hasText(userInfo.getSessionId())
                    && jwtBlacklistService.isBlacklisted(userInfo.getSessionId())) {
                SecurityContextHolder.clearContext();
                log.debug("JWT sessionId 已在黑名單，拒絕認證：{}", userInfo.getSessionId());
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userInfo, null, createAuthorities(userInfo.getRoleId()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (ExpiredJwtException e) {
            log.debug("JWT 已過期：{}", e.getMessage());
        } catch (Exception e) {
            log.debug("JWT 無效：{}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 從請求中提取 Token
     *
     * @param request
     * @return Token 字串，若無則返回 null
     */
    private String extractToken(HttpServletRequest request) {


        // 從 Cookie 提取 jwtToken
        String token = extractTokenFromCookie(request);
        if (StringUtils.hasText(token)) {
            log.debug("從 Cookie 提取 Token");
            return token;
        }

        return null;
    }

//    /**
//     * 從請求頭中提取Bearer Token
//     * @param request
//     * @return
//     */
//    private String extractBearerToken(HttpServletRequest request) {
//        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
//        if (!StringUtils.hasText(authorization)) {
//            return null;
//        }
//        if (authorization.startsWith("Bearer ")) {
//            return authorization.substring(7);
//        }
//        return authorization;
//    }

    /**
     * 從 Cookie 中提取 Token
     *
     * @param request HTTP 請求
     * @return Token 字串，若無則返回 null
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {

            if ("jwtToken".equals(cookie.getName())) {
                return cookie.getValue();
            }

        }
        return null;
    }

    private Collection<? extends GrantedAuthority> createAuthorities(@Nullable Role roleId) {
        if (roleId == null) {
            return List.of();
        }

        return switch (roleId) {
            case ADMIN -> List.of(
                    new SimpleGrantedAuthority("ROLE_ADMIN"),
                    new SimpleGrantedAuthority("ROLE_USER")
            );
            case NORMALUSER -> List.of(new SimpleGrantedAuthority("ROLE_USER"));
        };
    }

}

