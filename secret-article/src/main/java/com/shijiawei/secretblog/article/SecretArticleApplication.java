package com.shijiawei.secretblog.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;


@MapperScan("com.shijiawei.secretblog.article.mapper")
@ComponentScan(basePackages = {"com.shijiawei.secretblog.article", "com.shijiawei.secretblog.common"})
@SpringBootApplication
@EnableFeignClients
public class SecretArticleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecretArticleApplication.class, args);
	}

}
