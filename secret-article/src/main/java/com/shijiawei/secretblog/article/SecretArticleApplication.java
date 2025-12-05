package com.shijiawei.secretblog.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@MapperScan("com.shijiawei.secretblog.article.mapper")
@ComponentScan(basePackages = {"com.shijiawei.secretblog.article", "com.shijiawei.secretblog.common"})
@SpringBootApplication
@EnableFeignClients
@EnableScheduling
public class SecretArticleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecretArticleApplication.class, args);
	}

}
