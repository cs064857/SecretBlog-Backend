package com.shijiawei.secretblog.search;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.retry.annotation.EnableRetry;


@EnableRetry //啟用Spring Retry重試機制
@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(
        basePackages = {
                "com.shijiawei.secretblog.search",
                "com.shijiawei.secretblog.common",
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASPECTJ,
                pattern = "com.shijiawei.secretblog.common.utils.redis.*"//不載入Redis相關
        )
)
@SpringBootApplication
public class SecretSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretSearchApplication.class, args);
    }

}
