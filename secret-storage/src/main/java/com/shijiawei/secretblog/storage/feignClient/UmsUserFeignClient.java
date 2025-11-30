package com.shijiawei.secretblog.storage.feignClient;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.feign.dto.UmsUserAvatarUpdateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * ClassName: UmsUserFeignClient
 * Description:
 *
 * @Create 2024/10/31 上午6:10
 */
@Component
@FeignClient(name = "secret-user")
public interface UmsUserFeignClient {

    @PutMapping("/ums/user/update-avatar")
    R<Void> updateUmsUserAvatar(@RequestBody UmsUserAvatarUpdateDTO dto);

}
