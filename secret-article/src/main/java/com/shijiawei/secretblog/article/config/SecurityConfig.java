package com.shijiawei.secretblog.article.config;

import com.shijiawei.secretblog.article.filter.InternalApiKeyFilter;
import com.shijiawei.secretblog.common.security.JwtAuthenticationTokenFilter;
import com.shijiawei.secretblog.common.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Article微服務安全配置
 * 認證由下游服務自行驗證JWT(統一使用Authorization Bearer)
 */
@Configuration
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
                        .requestMatchers("/article/internal/**").permitAll()
                        .anyRequest().authenticated())
                //JWT驗證Filter
                .addFilterBefore(internalApiKeyFilter,UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new JwtAuthenticationTokenFilter(jwtService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
