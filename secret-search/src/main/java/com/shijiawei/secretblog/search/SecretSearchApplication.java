package com.shijiawei.secretblog.search;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@EnableRabbit
@EnableFeignClients
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.shijiawei.secretblog.search", "com.shijiawei.secretblog.common"})
@SpringBootApplication
public class SecretSearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecretSearchApplication.class, args);
    }

}
