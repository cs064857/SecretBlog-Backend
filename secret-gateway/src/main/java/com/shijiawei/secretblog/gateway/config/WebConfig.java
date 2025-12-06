package com.shijiawei.secretblog.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    @Bean
    public CorsWebFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);   // 允许cookies跨域
        config.addAllowedOrigin("http://localhost:51733");       // #允许向该服务器提交请求的URI，*表示全部允许
        config.addAllowedOrigin("http://localhost:5174");       // #允许向该服务器提交请求的URI，*表示全部允许
        config.addAllowedOrigin("http://localhost:3000");       // #允许向该服务器提交请求的URI，*表示全部允许
        config.addAllowedHeader("*");       // #允许访问的头信息,*表示全部
        config.addAllowedMethod("*");       // 允许提交请求的方法类型，*表示全部允许
        config.setMaxAge(18000L);           // 预检请求的缓存时间（秒），即在这个时间段里，对于相同的跨域请求不会再预检了
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(new PathPatternParser());
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }
}
