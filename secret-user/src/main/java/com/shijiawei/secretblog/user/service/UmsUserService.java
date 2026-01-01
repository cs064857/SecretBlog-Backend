package com.shijiawei.secretblog.user.service;

import java.util.List;

import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.feign.dto.UmsUserAvatarUpdateDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserSummaryDTO;
import com.shijiawei.secretblog.user.DTO.UmsChangePasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsForgotPasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsResetPasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsVerifyResetTokenDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;
import org.springframework.transaction.annotation.Transactional;

/**
* ClassName: UmsUserService
* Description:
* @Create 2024/9/14 上午3:57
*/
public interface UmsUserService extends IService<UmsUser>{


    R userLogin(UmsUserLoginDTO umsUserLoginDTO);

    /**
     * 修改用戶頭像
     * @param userId
     * @param avatar
     */
//    void updateAvatar(Long userId, String avatar);

    /**
     * 修改用戶暱稱
     * @param userId
     * @param nickName
     */
    void updateNickname(Long userId, String nickName);

    /**
     * 修改用戶性別
     * @param userId
     * @param gender
     */
    void updateGender(Long userId, Integer gender);

    /**
     * 依使用者 ID 取得使用者基本資料。
     * @param id 使用者 ID
     * @return 使用者資料；找不到時可能為 null
     */
    UmsUser selectByPrimaryKey(Long id);

    /**
     * 管理員新增用戶。
     * @param umsSaveUserVo 新增用戶資料
     */
    @Transactional(rollbackFor = Exception.class)
    void saveUmsUser(UmsSaveUserVo umsSaveUserVo);

    /**
     * 取得用戶明細清單。
     * @return 用戶明細 DTO 清單
     */
    List<UmsUserDetailsDTO> listUmsUserDetails();

    /**
     * 取得用戶清單。
     * @return 用戶清單
     */
    List<UmsUser> listUmsUser();

    /**
     * 批次刪除用戶，採用邏輯刪除
     * @param userIdList 用戶 ID 清單
     * @return
     */
    R deleteUserDetailsByIds(List<Long> userIdList);

    /**
     * 更新用戶細節資料。
     * @param updateUserDetailsVO 欲更新的欄位集合
     * @param userId 目標用戶 ID
     */
    void updateUmsUserDetails(UmsUpdateUserDetailsVO updateUserDetailsVO, Long userId);

    /**
     * 更新用戶頭像。
     * @param dto 頭像更新資料
     * @return
     */
    R<Void> updateUmsUserAvatar(UmsUserAvatarUpdateDTO dto);

    /**
     * 用戶註冊
     * @param umsUserRegisterDTO 註冊資料
     * @return
     */
    R UmsUserRegister(UmsUserRegisterDTO umsUserRegisterDTO);

    /**
     * 發送註冊用 Email 驗證碼。
     * @param umsUserEmailVerifyDTO Email 驗證請求
     * @return
     */
    R sendVerificationCode(UmsUserEmailVerifyDTO umsUserEmailVerifyDTO);

    /**
     * 批次查詢用戶基礎資訊
     * @param ids 用戶 ID 清單
     * @return 用戶基礎資訊清單
     */
    List<UserBasicDTO> selectUserBasicInfoByIds(List<Long> ids);

    /**
     * 刪除單一用戶，採用邏輯刪除
     *
     * @param userId 用戶 ID
     * @return
     */
    R deleteUserDetailsById(Long userId);

    /**
     * 取得用戶摘要資訊。
     * @param id 用戶 ID
     * @return 用戶摘要 DTO；找不到時可能為 null
     */
    UmsUserSummaryDTO getUserSummary(Long id);

    /**
     * 修改密碼（已登入用戶）
     * @param dto 修改密碼請求
     * @return
     */
    R changePassword(UmsChangePasswordDTO dto);

    /**
     * 發送忘記密碼驗證碼
     * @param dto 忘記密碼請求（包含 Email）
     * @return
     */
    R sendForgotPasswordCode(UmsForgotPasswordDTO dto);

    /**
     * 驗證密碼重設 Token 是否有效
     * @param token 重設 Token
     * @return
     */
    R verifyResetToken(String token);

    /**
     * 重設密碼
     * @param dto 重設密碼請求（包含 Token、新密碼）
     * @return
     */
    R resetPassword(UmsResetPasswordDTO dto);
}
