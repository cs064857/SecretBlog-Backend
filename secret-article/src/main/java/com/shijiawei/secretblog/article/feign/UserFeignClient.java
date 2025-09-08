package com.shijiawei.secretblog.article.feign;

import com.shijiawei.secretblog.article.config.FeignInterceptorConfig;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ClassName: UserFeignClient
 * Description: 用戶微服務遠程調用客戶端
 * @Create 2025/8/5 上午3:10
 *
 */
@FeignClient(name = "secret-user", path = "/ums/user",configuration = FeignInterceptorConfig.class)
public interface UserFeignClient {

    /**
     * 根據用戶ID獲取單個用戶信息
     * @param id 用戶ID
     * @return 用戶信息
     */
    @GetMapping("/selectOne")
    R<UserBasicDTO> getUserById(@RequestParam("id") Long id);

    /**
     * 根據用戶ID列表批量獲取用戶信息
     * @param ids 用戶ID列表
     * @return 用戶信息列表
     */
    @GetMapping("/list/byids")
    R<List<UserBasicDTO>> selectUserBasicInfoByIds(@RequestParam("ids") List<Long> ids);
//    R<List<UserBasicDTO>> getUsersByIds(@RequestParam("ids") List<Long> ids);




}
