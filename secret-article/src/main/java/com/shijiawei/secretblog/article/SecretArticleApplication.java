package com.shijiawei.secretblog.article;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.shijiawei.secretblog.article.mapper")
@SpringBootApplication
public class SecretArticleApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecretArticleApplication.class, args);
	}

}
