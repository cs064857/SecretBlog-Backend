package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.authentication.handler.login.UserLoginInfo;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.service.UmsUserService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

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

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 通過主鍵查詢單條數據
     *
     * @param id 主鍵
     * @return 單條數據
     */
    @GetMapping("selectOne")
    public R<UmsUser> getUserById(Long id) {
        UmsUser umsUser = umsUserService.selectByPrimaryKey(id);
        return R.ok(umsUser);
    }
    @GetMapping("list")
    public R<UmsUser> getUsersByIds(List<Long> ids) {
        UmsUser umsUser = umsUserService.selectUsersByIds(ids);
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

    /**
     * 用戶註冊帳號
     * @param umsUserRegisterDTO
     * @return
     */
    @PostMapping("/register")
    public R register(@Validated @RequestBody UmsUserRegisterDTO umsUserRegisterDTO){
        log.info("umsUserRegisterDTO:{}",umsUserRegisterDTO);
        return umsUserService.UmsUserRegister(umsUserRegisterDTO);
    }

    @PostMapping("/email-verify-code")
    public R sendVerificationCode(@RequestBody UmsUserEmailVerifyDTO umsUserEmailVerifyDTO){
        return umsUserService.sendVerificationCode(umsUserEmailVerifyDTO);
    }

//    @PostMapping("/login")
//    public R userLogin(@Validated UmsUserLoginDTO umsUserLoginDTO){
//        umsUserService.userLogin(umsUserLoginDTO);
//        return R.ok();
//    }


//    @GetMapping("/login")
//    public String index() {
//        return "index";
//    }

    @GetMapping("/login/business2")
    public R getA(){

        UserLoginInfo userLoginInfo = (UserLoginInfo)SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();
        System.out.println("自家登入信息："+ JSON.stringify(userLoginInfo));
        return new R("自家登入成功",userLoginInfo);
    }

    @PostMapping("/logout")
    public R logout(HttpServletRequest request, HttpServletResponse response) {
        // 清除安全上下文
        SecurityContextHolder.clearContext();

        // 可以在這裡添加將token加入黑名單的邏輯
        // 例如，將當前token存入Redis黑名單，設置過期時間為token剩餘有效期

        return R.ok("登出成功");
    }
}
