package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.JwtService; // TEMP 新增
import com.shijiawei.secretblog.common.utils.TimeTool;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserSummaryDTO;
import com.shijiawei.secretblog.user.authentication.handler.login.UserLoginInfo; // TEMP 新增
import com.shijiawei.secretblog.user.authentication.service.TokenBlacklistService; // TEMP 新增
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import com.shijiawei.secretblog.user.service.UmsUserService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;
import com.shijiawei.secretblog.user.converter.UserConverter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

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
    private UmsUserInfoService umsUserInfoService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private JwtService jwtService; // TEMP 新增

    @Autowired
    private TokenBlacklistService tokenBlacklistService; // TEMP 新增


    /**
     * 通過主鍵查詢單條數據
     *
     * @param id 主鍵
     * @return 單條數據
     */
    @GetMapping("selectOne")
    public R<UmsUser> getUserById(Long id) {
        UmsUser user = umsUserService.selectByPrimaryKey(id);
//        UmsUserInfo userInfo = umsUserInfoService.getOne(new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getUserId, id));
//        if(!user.isEmpty() && !userInfo.isEmpty()){
//            UserBasicDTO dto = new UserBasicDTO();
//            dto.setUserId(id);
//            dto.setNickName(user.getNickName());
//            dto.setAccountName(userInfo.getAccountName());
//            dto.setAvatar(user.getAvatar());
//            return R.ok(dto);
//        }
        if(user.isEmpty()){
//            throw new CustomRuntimeException("500","未能獲得用戶資料");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.USER_INTERNAL_ERROR)
                    .detailMessage("未能獲得用戶資料")
                    .data(Map.of("userId", ObjectUtils.defaultIfNull(id,"")))
                    .build();
        }

        return R.ok(user);
    }

    @GetMapping("/list/basic")
    public R<List<UserBasicDTO>> selectUserBasicInfoByIds(@RequestParam("ids") List<Long> ids) {

        List<UserBasicDTO> userBasicDTOS = umsUserService.selectUserBasicInfoByIds(ids);
        if(userBasicDTOS.isEmpty()){
//            throw new CustomRuntimeException("500","未能獲得用戶基本資料");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.USER_INTERNAL_ERROR)
                    .detailMessage("未能獲得用戶基本資料")
                    .data(Map.of("userIds", ObjectUtils.defaultIfNull(ids,"")))
                    .build();
        }
        log.info("userBasicDTOS:{}", userBasicDTOS);
        return R.ok(userBasicDTOS);

    }

    @GetMapping("/list/byids")
    public R<List<UmsUser>> getUsersByIds(@RequestParam("ids") List<Long> ids) {
        log.info("getUsersByIds.ids:{}",ids);
        List<UmsUser> umsUserList = umsUserService.listByIds(ids);
        return R.ok(umsUserList);
    }

    /**
     * 管理員新增帳號
     * @param umsSaveUserVo
     * @return
     */
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
    @GetMapping("/list")
    public R<List<UmsUser>> listUmsUser() {
        List<UmsUser> umsUserList = umsUserService.listUmsUser();
        return R.ok(umsUserList);
    }

    @GetMapping("/userDetails")
    public R<List<UmsUserDetailsDTO>> userDetails() {
        List<UmsUserDetailsDTO> umsUserDetailsDTOList = umsUserService.listUmsUserDetails();
        ///TODO 不要返回密碼
        return R.ok(umsUserDetailsDTOList);
    }
    @PostMapping("/delete/userDetail/{id}")
    public R deleteUserDetailsById( @PathVariable(name = "id") Long userId) {
        log.info("userId:{}",userId);
        return umsUserService.deleteUserDetailsById(userId);

    }
//    @DeleteMapping("/delete/userDetail")
//    public R deleteUserDetailsById(@RequestParam(name = "id") Long userId) {
//        log.info("userId:{}",userId);
//        R r = umsUserService.deleteUserDetailsById(userId);
//        return r;
//    }
    @DeleteMapping("/delete/userDetails")
    public R deleteUserDetailsByIds(@RequestParam(name = "ids") List<Long> userIdList) {
        log.info("userIdList:{}",userIdList);
        R r = umsUserService.deleteUserDetailsByIds(userIdList);
        return r;
    }
    @PutMapping("/userDetails/{userId}")
    public R updateUmsUserAndUserInfo(@RequestBody UmsUpdateUserDetailsVO updateUserDetailsVO, @PathVariable Long userId){
        log.info("updateUserDetailsVO:{}",updateUserDetailsVO);
        umsUserService.updateUmsUserDetails(updateUserDetailsVO,userId);
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

//    @GetMapping("/login/business2")
//    public R getA(){
//
//        UserLoginInfo userLoginInfo = (UserLoginInfo)SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getPrincipal();
//        System.out.println("自家登入信息："+ JSON.stringify(userLoginInfo));
//        return new R("自家登入成功",userLoginInfo);
//    }

    @PostMapping("/logout")
    public R logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        String originalJwtToken = null;
        // 1) 優先：從認證物件(MyJwtAuthentication)取出在過濾器中保存的原始JWT
        if (authentication instanceof com.shijiawei.secretblog.user.authentication.handler.login.business.MyJwtAuthentication auth) {
            originalJwtToken = auth.getJwtToken();
        }

        log.info("Logout originalJwtToken: {}", originalJwtToken);

        // 將當前 sessionId 放入黑名單（使用剩餘有效期作為TTL）
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserLoginInfo currentUser) {
                long now = TimeTool.nowMilli();
                long expiredTime = currentUser.getExpiredTime();
                long ttl = Math.max(expiredTime - now, 1000L); // 至少1秒，避免0或負數
                try {
                    tokenBlacklistService.blacklist(currentUser.getSessionId(), ttl);
                    log.info("SessionId {} 已加入黑名單, TTL={}ms", currentUser.getSessionId(), ttl);
                } catch (Exception e) {
                    log.warn("加入黑名單失敗: {}", e.getMessage(), e);
                }
            }
        }

        // 清除安全上下文
        SecurityContextHolder.clearContext();
        return R.ok("登出成功");
    }
    @GetMapping("/is-login")
    public R<String> isLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return new R<>("401", "未登入", null);
        }

        return R.ok("已登入", null);
    }

    @GetMapping("/summary/{id}")
    public R<UmsUserSummaryDTO> getUserSummary(@PathVariable Long id) {
        UmsUserSummaryDTO userSummary = umsUserService.getUserSummary(id);
        return R.ok(userSummary);
    }
}
