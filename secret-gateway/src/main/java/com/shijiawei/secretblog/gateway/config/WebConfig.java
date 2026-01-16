package com.shijiawei.secretblog.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.util.pattern.PathPatternParser;


/**
 * ClassName: WebConfig
 * Description:
 *
 * @Create 2024/8/26 上午2:35
 */
@Configuration
public class WebConfig{

    @Value("${app.cors.allowed-origin}")
    private String allowedOrigin;

    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);   // 允許cookies跨域
        config.addAllowedOrigin(allowedOrigin);       // #允許向該服務器提交請求的URI，*表示全部允許
        config.addAllowedHeader("*");       // #允許訪問的頭信息,*表示全部
        config.addAllowedMethod("*");       // 允許提交請求的方法類型，*表示全部允許
        config.setMaxAge(18000L);           // 預檢請求的緩存時間（秒），即在這個時間段裡，對於相同的跨域請求不會再預檢了
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
