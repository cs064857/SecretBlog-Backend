package com.shijiawei.secretblog.storage.feignClient;

import com.shijiawei.secretblog.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * ClassName: UmsUserFeignClient
 * Description:
 *
 * @Create 2024/10/31 上午6:10
 */
@Component
@FeignClient(name = "secret-user")
public interface UmsUserFeignClient {

    @PutMapping("/imgUrl/{userId}/{imgUrl}")
    void updateUmsUserAndUserInfo(@PathVariable String userId, @PathVariable String imgUrl);

}
