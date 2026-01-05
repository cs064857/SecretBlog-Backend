package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.security.JwtService;
import com.shijiawei.secretblog.common.feign.dto.UmsUserAvatarUpdateDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserSummaryDTO;
import com.shijiawei.secretblog.user.DTO.UmsChangePasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsForgotPasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsResetPasswordDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import com.shijiawei.secretblog.user.service.UmsUserService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;
import com.shijiawei.secretblog.user.converter.UserConverter;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RedissonClient;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * (ums_user)表控制層
 *
 */
@Slf4j
@RestController
@Tag(name = "使用者管理", description = "使用者相關的 CRUD 操作")
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

//    @Autowired
//    private TokenBlacklistService tokenBlacklistService;

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
//
//    @PutMapping("/{userId}/avatar")
//    public R<Void> updateAvatar(@PathVariable Long userId, @RequestParam String avatar) {
//        umsUserService.updateAvatar(userId, avatar);
//        return R.ok();
//    }

    @PutMapping("/{userId}/nickname")
    public R<Void> updateNickname(@PathVariable Long userId, @RequestParam String nickname) {
        umsUserService.updateNickname(userId, nickname);
        return R.ok();
    }

    @PutMapping("/{userId}/gender")
    public R<Void> updateGender(@PathVariable Long userId, @RequestParam Integer gender) {
        umsUserService.updateGender(userId, gender);
        return R.ok();
    }

    @PutMapping("/{userId}/notify-enabled")
    public R<Void> updateNotifyEnabled(@PathVariable Long userId, @RequestParam("notifyEnabled") Byte notifyEnabled) {
        umsUserService.updateNotifyEnabled(userId, notifyEnabled);
        return R.ok();
    }

    @PutMapping("/update-avatar")
    public R<Void> updateUmsUserAvatar(@RequestBody UmsUserAvatarUpdateDTO dto){
        log.info("updateUmsUserAvatar dto:{}", dto);
        return umsUserService.updateUmsUserAvatar(dto);

    }

//    @PutMapping("/update-avatar")
//    public R<Void> updateUmsUserAvatar(@RequestBody UmsUserAvatarUpdateDTO dto){
//        log.info("updateUmsUserAvatar dto:{}", dto);
//        return umsUserService.updateUmsUserAvatar(dto.getAvatar(), dto.getUserId().toString());
//    }

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

    @GetMapping("/summary/{id}")
    public R<UmsUserSummaryDTO> getUserSummary(@PathVariable Long id) {
        UmsUserSummaryDTO userSummary = umsUserService.getUserSummary(id);
        return R.ok(userSummary);
    }

    /**
     * 修改密碼（需認證）
     * @param dto 修改密碼請求
     * @return 結果
     */
    @PutMapping("/change-password")
    public R changePassword(@Validated @RequestBody UmsChangePasswordDTO dto) {
        return umsUserService.changePassword(dto);
    }

    /**
     * 忘記密碼 - 請求發送重設連結
     * @param dto 忘記密碼請求（包含 Email）
     * @return 結果
     */
    @PostMapping("/forgot-password")
    public R forgotPassword(@Validated @RequestBody UmsForgotPasswordDTO dto) {
        return umsUserService.sendForgotPasswordCode(dto);
    }

    /**
     * 驗證密碼重設 Token 是否有效
     * @param token 重設 Token
     * @return 結果
     */
    @GetMapping("/verify-reset-token")
    public R verifyResetToken(@RequestParam String token) {
        return umsUserService.verifyResetToken(token);
    }

    /**
     * 重設密碼
     * @param dto 重設密碼請求（包含 Token、新密碼）
     * @return 結果
     */
    @PostMapping("/reset-password")
    public R resetPassword(@Validated @RequestBody UmsResetPasswordDTO dto) {
        return umsUserService.resetPassword(dto);
    }
}
