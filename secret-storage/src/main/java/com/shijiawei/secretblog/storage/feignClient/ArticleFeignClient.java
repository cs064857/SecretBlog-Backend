package com.shijiawei.secretblog.storage.feignClient;

import com.shijiawei.secretblog.common.feign.dto.AmsAuthorAvatarUpdateDTO;
import com.shijiawei.secretblog.common.utils.R;
import lombok.Data;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "secret-article", path = "/article")
public interface ArticleFeignClient {

    @PostMapping("/internal/user/update-info")
    R<Void> updateAuthorInfo(@RequestBody AmsAuthorUpdateDTO dto);

    @PostMapping("/internal/user/update-avatar")
    R<Void> updateAuthorAvatar(@RequestBody AmsAuthorAvatarUpdateDTO dto);

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
