package com.shijiawei.secretblog.storage.feignClient;

import com.shijiawei.secretblog.common.feign.dto.AmsAuthorAvatarUpdateDTO;
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

    @PutMapping("/ams/internal/users/{userId}/avatar")
    R<Void> updateAuthorAvatar(@PathVariable("userId") Long userId, @RequestBody AmsAuthorAvatarUpdateDTO dto);

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
