package com.shijiawei.secretblog.article.config;

import com.shijiawei.secretblog.article.filter.InternalApiKeyFilter;
import com.shijiawei.secretblog.common.security.JwtAuthenticationTokenFilter;
import com.shijiawei.secretblog.common.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Article微服務安全配置
 * 認證由下游服務自行驗證JWT(統一使用Authorization Bearer)
 */
@Configuration
@EnableMethodSecurity
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private InternalApiKeyFilter internalApiKeyFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService) throws Exception {
        http
                //禁用 CSRF 保護
                .csrf(AbstractHttpConfigurer::disable)
                //禁用不需要的預設登入方式
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                //無狀態(JWT)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //授權規則
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        //SEO：由Gatewa 轉發到article的root路徑
                        .requestMatchers("/sitemap.xml", "/robots.txt", "/favicon.ico").permitAll()
                        //開放給微服務間調用
                        .requestMatchers("/ams/internal/articles/**", "/ams/internal/users/**").permitAll()
                        //公開的 GET 端點
                        .requestMatchers(HttpMethod.GET, "/ams/articles", "/ams/articles/*", "/ams/articles/*/action-status", "/ams/articles/*/comments", "/ams/articles/*/comments/action-status", "/ams/articles/*/translations", "/ams/categories/tree", "/ams/categories/*/articles", "/ams/tags", "/ams/articles/*/tags", "/ams/users/*/comments").permitAll()
                        //已認證用戶可存取的端點（PUT編輯文章、編輯留言）
                        .requestMatchers(HttpMethod.PUT, "/ams/articles/*", "/ams/comments/*").authenticated()
                        //分類管理端點：需管理員權限
                        .requestMatchers("/ams/categories/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                //JWT驗證Filter
                .addFilterBefore(internalApiKeyFilter,UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationTokenFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
