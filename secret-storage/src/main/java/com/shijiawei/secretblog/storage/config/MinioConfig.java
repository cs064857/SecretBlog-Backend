package com.shijiawei.secretblog.storage.config;

import io.minio.MinioClient;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName: MinioConfig
 * Description:
 *
 * @Create 2024/10/27 下午11:43
 */
@Configuration
public class MinioConfig {

    @Value("${var.secretblog.accessKey}")
    private String accessKey;
    @Value("${var.secretblog.secretKey}")
    private String secretKey;
    @Value("${var.endPoint}")
    private String endPoint;
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endPoint)
                .credentials(accessKey, secretKey)
                .build();
    }

}
