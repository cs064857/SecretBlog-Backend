package com.shijiawei.secretblog.gateway.authentication.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.gateway.authentication.handler.AuthorizationHelper;
import com.shijiawei.secretblog.gateway.authentication.handler.login.business.EnhancedJwtAuthenticationFilter;
import com.shijiawei.secretblog.gateway.config.JwtService;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final JwtService jwtService;
    private final AuthorizationHelper authorizationHelper;

    public SecurityConfig(JwtService jwtService, AuthorizationHelper authorizationHelper) {
        this.jwtService = jwtService;
        this.authorizationHelper = authorizationHelper;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        // 自定義認證失敗處理器
        ServerAuthenticationEntryPoint authenticationEntryPoint = (exchange, ex) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            String body = JSON.stringify(R.error("認證失敗：請先登入"));
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
            return response.writeWith(Mono.just(buffer));
        };

        // 自定義授權失敗處理器
        ServerAccessDeniedHandler accessDeniedHandler = (exchange, denied) -> {
            ServerHttpResponse response = exchange.getResponse();
            response.setStatusCode(HttpStatus.FORBIDDEN);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            String body = JSON.stringify(R.error("權限不足：您無權訪問此資源"));
            DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
            return response.writeWith(Mono.just(buffer));
        };

        // 配置安全策略
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .logout(ServerHttpSecurity.LogoutSpec::disable)
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler))
            .authorizeExchange(authorizeExchange -> authorizeExchange
                // 開放登入/註冊等匿名端點
                .pathMatchers(
                    "/api/ums/user/login/**",
                    "/api/ums/user/register",
                    "/api/ums/user/email-verify-code",
                    "/api/public/**",
                    "/actuator/**",
                    "/favicon.ico",
                    "/sitemap.xml",    // SEO: sitemap
                    "/robots.txt"      // SEO: robots
                ).permitAll()
                
                // 管理員專屬路由
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .pathMatchers("/api/ums/user/delete/**").hasRole("ADMIN")
                .pathMatchers("/api/ums/role/**").hasRole("ADMIN")
                .pathMatchers("/api/ums/status/**").hasRole("ADMIN")
                
                // 需要認證的用戶路由
                .pathMatchers("/api/ums/user/profile/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/ums/user/update/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/article/**").hasAnyRole("USER", "ADMIN")
                .pathMatchers("/api/sms/**").hasAnyRole("USER", "ADMIN")
                
                // 其餘路由需要認證
                .pathMatchers("/api/**").authenticated()
                .anyExchange().permitAll())
            
            // 使用增強型 JWT 過濾器替換原有的 ReactiveJwtAuthenticationFilter
            .addFilterAt(new EnhancedJwtAuthenticationFilter(jwtService), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    /**
     * 密碼加密使用的編碼器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}