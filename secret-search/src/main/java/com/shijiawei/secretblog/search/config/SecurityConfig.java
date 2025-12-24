package com.shijiawei.secretblog.search.config;

import com.shijiawei.secretblog.common.security.JwtAuthenticationTokenFilter;
import com.shijiawei.secretblog.common.security.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Search 微服務安全配置
 * 認證由下游服務自行驗證 JWT（統一使用 Authorization Bearer）
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
            // 禁用 CSRF 保護
            .csrf(AbstractHttpConfigurer::disable)
            // 禁用不需要的預設登入方式
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .logout(AbstractHttpConfigurer::disable)
            // 無狀態（JWT）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授權規則（Search 服務預設皆需登入）
            .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/actuator/**").permitAll()
                    .anyRequest().authenticated()
            )
            // JWT 驗證 Filter
            .addFilterBefore(new JwtAuthenticationTokenFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
