package com.shijiawei.secretblog.user.feign;

import com.shijiawei.secretblog.common.utils.R;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "secret-article")
public interface ArticleFeignClient {

    @PutMapping("/ams/internal/users/{userId}/info")
    R<Void> updateAuthorInfo(@PathVariable("userId") Long userId, @RequestBody AmsAuthorUpdateDTO dto);

    @Data
    class AmsAuthorUpdateDTO {
        private Long userId;
        private String nickName;
        private String avatar;

        public AmsAuthorUpdateDTO(Long userId, String nickName, String avatar) {
            this.userId = userId;
            this.nickName = nickName;
            this.avatar = avatar;
        }
    }
}
