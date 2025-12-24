package com.shijiawei.secretblog.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@EnableFeignClients
@ComponentScan(basePackages = {"com.shijiawei.secretblog.storage", "com.shijiawei.secretblog.common"})
@SpringBootApplication
public class SecretStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretStorageApplication.class, args);
    }

}
