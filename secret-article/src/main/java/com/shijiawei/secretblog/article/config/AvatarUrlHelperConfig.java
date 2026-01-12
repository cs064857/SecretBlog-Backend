package com.shijiawei.secretblog.article.config;

import com.shijiawei.secretblog.user.utils.AvatarUrlHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Avatar URL/路徑轉換工具設定。
 * 目的:讓 secret-article 也能在入庫/出庫時統一處理 MinIO 頭像 URL。
 */
@Configuration
public class AvatarUrlHelperConfig {

    @Bean
    public AvatarUrlHelper avatarUrlHelper(@Value("${custom.minio-domain:}") String minioDomain) {
        return new AvatarUrlHelper(minioDomain);
    }
}

