package com.shijiawei.secretblog.minio.config;

import io.minio.MinioClient;
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

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint("http://192.168.26.5:9000")
                .credentials("p6KMsat29WtFgrWPasyR", "bNgTw96toBr5LbGrzKFnL4oXIV0g7khBYuPB4Zhs")
                .build();
    }

}
