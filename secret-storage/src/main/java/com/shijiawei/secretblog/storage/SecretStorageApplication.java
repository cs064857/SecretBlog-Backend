package com.shijiawei.secretblog.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@EnableFeignClients
@ComponentScan(
        basePackages = {
                "com.shijiawei.secretblog.storage",
                "com.shijiawei.secretblog.common.utils",
                "com.shijiawei.secretblog.common.security",
                "com.shijiawei.secretblog.common.exception"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASPECTJ,
                pattern = "com.shijiawei.secretblog.common.utils.redis.*"//不載入Redis相關
        )
)
@SpringBootApplication
public class SecretStorageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretStorageApplication.class, args);
    }

}
