package com.shijiawei.secretblog.storage.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
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
                    // 統一透傳 Authorization Bearer，供下游服務自行驗證 Token
                    template.header("Authorization", authorization);
                }
            }
        }
    }

}
