package com.shijiawei.secretblog.user.service;

import java.util.List;

import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserSummaryDTO;
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
    void updateAvatar(Long userId, String avatar);

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

    UmsUser selectByPrimaryKey(Long id);

    @Transactional(rollbackFor = Exception.class)
    void saveUmsUser(UmsSaveUserVo umsSaveUserVo);

    List<UmsUserDetailsDTO> listUmsUserDetails();

    List<UmsUser> listUmsUser();

    R deleteUserDetailsByIds(List<Long> userIdList);

    void updateUmsUserDetails(UmsUpdateUserDetailsVO updateUserDetailsVO, Long userId);

    R<Void> updateUmsUserAvatar(String imgUrl, String userId);

    R UmsUserRegister(UmsUserRegisterDTO umsUserRegisterDTO);

    R sendVerificationCode(UmsUserEmailVerifyDTO umsUserEmailVerifyDTO);

    List<UserBasicDTO> selectUserBasicInfoByIds(List<Long> ids);

    R deleteUserDetailsById(Long userId);

    UmsUserSummaryDTO getUserSummary(Long id);
}
