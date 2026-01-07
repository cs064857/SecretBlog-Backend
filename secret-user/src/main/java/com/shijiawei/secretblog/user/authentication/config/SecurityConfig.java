package com.shijiawei.secretblog.user.authentication.config;

import com.shijiawei.secretblog.common.security.JwtAuthenticationTokenFilter;
import com.shijiawei.secretblog.common.security.JwtService;
import com.shijiawei.secretblog.user.authentication.handler.exception.CustomAuthenticationExceptionHandler;
import com.shijiawei.secretblog.user.authentication.handler.exception.CustomAuthorizationExceptionHandler;
import com.shijiawei.secretblog.user.authentication.handler.exception.oauth2.OAuth2LoginSuccessHandler;
import com.shijiawei.secretblog.user.authentication.service.TokenBlacklistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * ClassName: SecurityConfig
 * Description:
 *
 * @Create 2025/12/22 下午11:55
 */
@EnableMethodSecurity//開啟權限校驗功能
@Configuration
@EnableWebSecurity//開啟SpringSecurity基本功能
public class SecurityConfig {

    @Autowired
    AuthenticationConfiguration authenticationConfiguration;//獲取AuthenticationManager

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;


    @Autowired
    private CustomAuthenticationExceptionHandler customAuthenticationExceptionHandler;

    @Autowired
    private CustomAuthorizationExceptionHandler customAuthorizationExceptionHandler;

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; //

    @Bean
    public PasswordEncoder passwordEncoder (){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 配置Spring Security的過濾鏈。
     *
     * @param http 用於構建安全配置的HttpSecurity對象。
     * @return 返回配置好的SecurityFilterChain對象。
     * @throws Exception 如果配置過程中發生錯誤，則拋出異常。
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用CSRF保護
                .csrf(AbstractHttpConfigurer::disable)
                // 設置會話創建策略為無狀態
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 配置授權規則（登入端點放行，其餘需要身份認證）
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/ums/user/login/username").permitAll()
                        // 公開端點：註冊、傳送驗證碼、忘記密碼
                        .requestMatchers("/ums/user/register", "/ums/user/email-verify-code","/ums/user/reset-password","/ums/user/verify-reset-token", "/ums/user/forgot-password").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/me").authenticated()
                        .requestMatchers(
                                "/oauth2/**",          // OAuth2 登入觸發點 (如 /oauth2/authorization/google)
                                "/login/oauth2/code/*"// OAuth2 登入成功後的 redirect url
                        ).permitAll()
                        // 管理員端點
                        .requestMatchers(
                                "/ums/role/**",
                                "/ums/status/**",
                                "/ums/auths/**",
                                "/ums/credentials/**",
                                "/ums/user/delete/**"
                        ).hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // 開啟跨域訪問
                .cors(AbstractHttpConfigurer::disable)
                .oauth2Login(oauth2 -> oauth2.successHandler(oAuth2LoginSuccessHandler))
                // 添加JWT認證過濾器（統一 Authorization Bearer）
                .addFilterBefore(new JwtAuthenticationTokenFilter(jwtService, tokenBlacklistService), UsernamePasswordAuthenticationFilter.class)
                // 配置例外處理
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(customAuthenticationExceptionHandler)
                        .accessDeniedHandler(customAuthorizationExceptionHandler)
                )
                // 交由 Controller 實作 /logout（並於 Controller 寫入黑名單）
                .logout(AbstractHttpConfigurer::disable);



        // 構建並返回安全過濾鏈
        return http.build();
    }
}
