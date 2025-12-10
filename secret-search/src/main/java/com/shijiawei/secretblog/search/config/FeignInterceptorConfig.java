package com.shijiawei.secretblog.search.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Feign 攔截器配置
 * 用於在微服務間調用時傳遞認證資訊（Authorization Header / Cookie）
 */
@Configuration
public class FeignInterceptorConfig {

    /**
     * Feign 請求攔截器
     * @return RequestInterceptor 實例
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new FeignRequestInterceptor();
    }

    /**
     * 實現攔截器 RequestInterceptor
     * 自動傳遞 Authorization Header 或 Cookie 中的 jwtToken
     */
    static class FeignRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate template) {
            ServletRequestAttributes servletRequestAttributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            
            if (servletRequestAttributes != null) {
                HttpServletRequest request = servletRequestAttributes.getRequest();
                
                // 優先傳遞 Authorization Header
                String authorization = request.getHeader("Authorization");
                if (StringUtils.hasText(authorization)) {
                    template.header("Authorization", authorization);
                    return;
                }
                
                // Authorization 為空時，回退至 Cookie 中的 jwtToken
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwtToken".equals(cookie.getName()) && StringUtils.hasText(cookie.getValue())) {
                            String jwt = cookie.getValue();
                            // 1) 透傳 Cookie，滿足下游只讀取 Cookie 的驗證邏輯
                            template.header("Cookie", "jwtToken=" + jwt);
                            // 2) 同時補一個 Bearer 頭，便於其他服務基於 Authorization 校驗
                            template.header("Authorization", "Bearer " + jwt);
                            break;
                        }
                    }
                }
            }
        }
    }
}
