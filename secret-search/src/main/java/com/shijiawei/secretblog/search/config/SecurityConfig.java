package com.shijiawei.secretblog.search.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Search 微服務安全配置
 * 禁用 Spring Security 的默認行為，因為認證由 Gateway 處理
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF 保護
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用表單登入
            .formLogin(AbstractHttpConfigurer::disable)
            // 禁用 HTTP Basic 認證
            .httpBasic(AbstractHttpConfigurer::disable)
            // 禁用登出功能
            .logout(AbstractHttpConfigurer::disable)
            // 禁用會話管理
            .sessionManagement(AbstractHttpConfigurer::disable)
            // 允許所有請求（認證由 Gateway 處理）
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            );

        return http.build();
    }
}
