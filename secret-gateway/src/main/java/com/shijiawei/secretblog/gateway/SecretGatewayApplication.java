package com.shijiawei.secretblog.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// 排除 Servlet Security 自動配置，Gateway 使用 WebFlux Security
@SpringBootApplication(exclude = {
    SecurityAutoConfiguration.class,
    UserDetailsServiceAutoConfiguration.class
})
public class SecretGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretGatewayApplication.class, args);
    }

}
