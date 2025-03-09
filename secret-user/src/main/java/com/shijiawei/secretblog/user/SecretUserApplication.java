package com.shijiawei.secretblog.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.shijiawei.secretblog.user", "com.shijiawei.secretblog.common"})
public class SecretUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretUserApplication.class, args);
    }

}
