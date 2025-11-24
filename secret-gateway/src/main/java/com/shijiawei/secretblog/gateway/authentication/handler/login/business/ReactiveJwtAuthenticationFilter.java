package com.shijiawei.secretblog.gateway.authentication.handler.login.business;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpCookie;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.shijiawei.secretblog.gateway.authentication.handler.login.UserLoginInfo;
import com.shijiawei.secretblog.gateway.config.JwtService;

import io.jsonwebtoken.ExpiredJwtException;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ReactiveJwtAuthenticationFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveJwtAuthenticationFilter.class);

    private final JwtService jwtService;

    public ReactiveJwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        logger.debug("Use ReactiveJwtAuthenticationFilter");
        
        ServerHttpRequest request = exchange.getRequest();
        
        // 從 cookie 中獲取 token
        String jwtToken = null;
        HttpCookie cookie = request.getCookies().getFirst("jwtToken");
        if (cookie != null) {
            jwtToken = cookie.getValue();
        }
        
        logger.debug("JWT token from cookie: {}", jwtToken);
        
        if (!StringUtils.hasText(jwtToken)) {
            return chain.filter(exchange);
        }
        
        try {
            UserLoginInfo userLoginInfo = jwtService.verifyJwt(jwtToken, UserLoginInfo.class);
            
            // 創建認證對象
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userLoginInfo, null, userLoginInfo.getAuthorities());
            
            // 設置認證信息到上下文
            return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (ExpiredJwtException e) {
            // JWT 過期
//            logger.debug("JWT token expired: {}", e.getMessage());
//            throw new CustomRuntimeException(ResultCode.JWT_CONFIG_ERROR.getCode(), ResultCode.JWT_CONFIG_ERROR.getMessage(), e.getCause());
//            return Mono.empty();
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                    .cause(e.getCause())
                    .detailMessage("Jwt過期")
                    .build();
        } catch (Exception e) {
            // JWT 無效
//            logger.debug("Invalid JWT token: {}", e.getMessage());
//            return Mono.empty();
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                    .cause(e.getCause())
                    .detailMessage("Invalid JWT token")
                    .build();
        }
    }
} 