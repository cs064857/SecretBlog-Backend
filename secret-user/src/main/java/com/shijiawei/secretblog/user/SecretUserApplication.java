package com.shijiawei.secretblog.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.shijiawei.secretblog.user", "com.shijiawei.secretblog.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableScheduling
public class SecretUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretUserApplication.class, args);
    }

}
