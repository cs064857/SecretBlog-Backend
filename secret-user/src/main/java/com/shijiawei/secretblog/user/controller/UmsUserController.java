package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.service.Impl.UmsUserServiceImpl;
import com.shijiawei.secretblog.user.service.UmsUserService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * (ums_user)表控制層
 *
 * @author xxxxx
 */
@Slf4j
@RestController
@RequestMapping("/ums/user")
public class UmsUserController {
    /**
     * 服務對象
     */
    @Autowired
    private UmsUserService umsUserService;

    /**
     * 通過主鍵查詢單條數據
     *
     * @param id 主鍵
     * @return 單條數據
     */
    @GetMapping("selectOne")
    public R<UmsUser> selectOne(Integer id) {
        UmsUser umsUser = umsUserService.selectByPrimaryKey(id);
        return R.ok(umsUser);
    }

    @PostMapping
    public R saveUmsUser(@RequestBody UmsSaveUserVo umsSaveUserVo) {
        log.info("umsSaveUserVo:{}",umsSaveUserVo);
        umsUserService.saveUmsUser(umsSaveUserVo);
        return R.ok();
    }

    @PutMapping
    public void updateUmsUserAvatar(@RequestParam String imgUrl,@RequestParam String userId){
        log.info("imgUrl:{}",imgUrl);
        umsUserService.updateUmsUserAvatar(imgUrl,userId);
    }
    /**
     * 獲取所有使用者
     *
     * @return
     */
    @GetMapping("/user")
    public R<List<UmsUser>> listUmsUser() {
        List<UmsUser> umsUserList = umsUserService.listUmsUser();
        return R.ok(umsUserList);
    }

    @GetMapping("/userDetails")
    public R<List<UmsUserDetailsDTO>> userDetails() {
        List<UmsUserDetailsDTO> umsUserDetailsDTOList = umsUserService.listUmsUserDetails();
        return R.ok(umsUserDetailsDTOList);
    }

    @DeleteMapping("/userDetails/{id}")
    public R deleteUmsUserAndUserInfo(@PathVariable(name = "id") List<Long> userIdList) {
        log.info("userIdList:{}",userIdList);
        umsUserService.deleteUmsUserDetails(userIdList);
        return R.ok();
    }
    @PutMapping("/userDetails/{userId}/{userInfoId}")
    public R updateUmsUserAndUserInfo(@RequestBody UmsUpdateUserDetailsVO updateUserDetailsVO, @PathVariable Long userId, @PathVariable Long userInfoId){
        log.info("updateUserDetailsVO:{}",updateUserDetailsVO);
        umsUserService.updateUmsUserDetails(updateUserDetailsVO,userId,userInfoId);
        return R.ok();
    }

}
