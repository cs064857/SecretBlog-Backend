package com.shijiawei.secretblog.user.authentication.config;


import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.shijiawei.secretblog.user.authentication.handler.exception.CustomAuthenticationExceptionHandler;
import com.shijiawei.secretblog.user.authentication.handler.exception.CustomAuthorizationExceptionHandler;
import com.shijiawei.secretblog.user.authentication.handler.exception.CustomSecurityExceptionHandler;
import com.shijiawei.secretblog.user.authentication.handler.login.LoginFailHandler;
import com.shijiawei.secretblog.user.authentication.handler.login.LoginSuccessHandler;
import com.shijiawei.secretblog.user.authentication.handler.login.business.MyJwtAuthenticationFilter;
import com.shijiawei.secretblog.user.authentication.handler.login.username.UsernameAuthenticationFilter;
import com.shijiawei.secretblog.user.authentication.handler.login.username.UsernameAuthenticationProvider;
import com.shijiawei.secretblog.user.authentication.service.JwtService;

import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApplicationContext applicationContext;

    public SecurityConfig(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    //處理異常
    private final AuthenticationEntryPoint authenticationExceptionHandler = new CustomAuthenticationExceptionHandler();
    //處理異常
    private final AccessDeniedHandler authorizationExceptionHandler = new CustomAuthorizationExceptionHandler();
    //處理異常
    private final Filter globalSpringSecurityExceptionHandler = new CustomSecurityExceptionHandler();


    public void commonHttpSetting(HttpSecurity http) throws Exception {
        /**
         * 關閉非前後端分離時所需的或者不需要的過濾器
         */
        http
                .securityMatcher("/article/comment/**")
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(AbstractHttpConfigurer::disable)
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .anonymous(AbstractHttpConfigurer::disable);


        // 處理 SpringSecurity 異常響應結果。響應數據的結構，改成業務統一的JSON結構。不要框架默認的響應結構
        http.exceptionHandling(exceptionHandling ->
                exceptionHandling
                        // 認證失敗異常
                        .authenticationEntryPoint(authenticationExceptionHandler)
                        // 鑒權失敗異常
                        .accessDeniedHandler(authorizationExceptionHandler)
        );
        // 其他未知異常. 盡量提前加載。
        http.addFilterBefore(globalSpringSecurityExceptionHandler, SecurityContextHolderFilter.class);
    }

    @Bean
    public SecurityFilterChain LoginApiFilterChain(HttpSecurity http) throws Exception {
        //關閉不需要的過濾器
        commonHttpSetting(http);

        /**
         * 必要的配置，決定哪些請求需要登入
         */
        http
                .securityMatcher("/ums/user/login/username")
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                );

        LoginSuccessHandler loginSuccessHandler = applicationContext.getBean(LoginSuccessHandler.class);
        LoginFailHandler loginFailHandler = applicationContext.getBean(LoginFailHandler.class);


        String usernameLoginPath = "/ums/user/login/username";
        // 加一個登錄方式。用戶名、密碼登錄
        UsernameAuthenticationFilter usernameLoginFilter = new UsernameAuthenticationFilter(
                new AntPathRequestMatcher(usernameLoginPath, HttpMethod.POST.name()),
                new ProviderManager(
                        List.of(applicationContext.getBean(UsernameAuthenticationProvider.class))),
                loginSuccessHandler,
                loginFailHandler);
        /**
         * 將自訂的過濾器加至過濾鏈中，在UsernamePasswordAuthenticationFilter之前(LogoutFilter之後)
         */
        http.addFilterBefore(usernameLoginFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();

    }

//    @Bean
//    public SecurityFilterChain Business2ApiFilterChain(HttpSecurity http) throws Exception {
//        //關閉不需要的過濾器
//        commonHttpSetting(http);
//
//        /**
//         * 必要的配置，決定哪些請求需要登入
//         */
//        http
//                .securityMatcher("/ums/user/login/business2","/article/articles/**")
//                .authorizeHttpRequests(authorize -> authorize
//                        .anyRequest().authenticated()
//                );
//
////        String business2LoginPath = "/ums/user/login/business2";
//        // 加一個登錄方式。用戶名、密碼登錄
//        MyJwtAuthenticationFilter filter = new MyJwtAuthenticationFilter(applicationContext.getBean(JwtService.class));
//        /**
//         * 將自訂的過濾器加至過濾鏈中，在UsernamePasswordAuthenticationFilter之前(LogoutFilter之後)
//         */
//        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    /**
     * 文章API安全配置
     * 保護/article/articles/**路徑，使其只有登錄用戶才能訪問
     */
//    @Bean
//    public SecurityFilterChain ArticleApiFilterChain(HttpSecurity http) throws Exception {
//        //關閉不需要的過濾器
//        commonHttpSetting(http);
//
//        /**
//         * 必要的配置，決定哪些請求需要登入
//         */
//        http
//                .securityMatcher("/article/articles/**")
//                .authorizeHttpRequests(authorize -> authorize
//                        .anyRequest().authenticated()
//                );
//
//        // 使用JWT認證過濾器
//        MyJwtAuthenticationFilter filter = new MyJwtAuthenticationFilter(applicationContext.getBean(JwtService.class));
//        /**
//         * 將自訂的過濾器加至過濾鏈中，在UsernamePasswordAuthenticationFilter之前(LogoutFilter之後)
//         */
//        http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    /**
     * 密碼加密使用的編碼器
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}