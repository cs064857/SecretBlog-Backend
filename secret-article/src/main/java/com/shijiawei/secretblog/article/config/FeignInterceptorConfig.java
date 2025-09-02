package com.shijiawei.secretblog.article.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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