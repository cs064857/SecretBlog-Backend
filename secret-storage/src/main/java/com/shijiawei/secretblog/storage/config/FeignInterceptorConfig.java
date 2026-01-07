package com.shijiawei.secretblog.storage.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignInterceptorConfig {

    /**
     * feign 攔截器
     * @return RequestInterceptor 實例
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new MyRequestInterceptor();
    }

    /**
     * 實現攔截器RequestInterceptor
     */
    static class MyRequestInterceptor implements RequestInterceptor {
        @Override
        public void apply(RequestTemplate template) {
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (servletRequestAttributes != null) {
                HttpServletRequest request = servletRequestAttributes.getRequest();

                // 1. 優先嘗試從原始請求，則從 Cookie 中尋找 jwtToken
                Cookie[] cookies = request.getCookies();
                if (cookies != null) {
                    for (Cookie cookie : cookies) {
                        if ("jwtToken".equals(cookie.getName())) {
                            // 轉換為標準的 Bearer Token 格式透傳給下游服務
                            template.header("Authorization", "Bearer " + cookie.getValue());
                            return;
                        }
                    }
                }
                // 2. 嘗試從原始請求的 Authorization Header 獲取
                String authorization = request.getHeader("Authorization");
                if (StringUtils.hasText(authorization)) {
                    template.header("Authorization", authorization);
                }
            }
        }
    }

}
