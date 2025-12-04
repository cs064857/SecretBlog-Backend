package com.shijiawei.secretblog.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.shijiawei.secretblog.user.feign")
@ComponentScan(basePackages = {"com.shijiawei.secretblog.user", "com.shijiawei.secretblog.common"})
@EnableScheduling
public class SecretUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretUserApplication.class, args);
    }

}
