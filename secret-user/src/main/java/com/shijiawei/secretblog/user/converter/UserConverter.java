package com.shijiawei.secretblog.user.converter;

import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.utils.AvatarUrlHelper;

import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 用戶實體與DTO轉換器
 */
@Component
public class UserConverter {

    @Value("${custom.minio-domain}")
    private String minioDomain;

    /**
     * 將實體轉換為DTO
     */
    public UserBasicDTO toDTO(UmsUser umsUser, UmsUserInfo userInfo) {
        if (umsUser == null) {
            return null;
        }

        UserBasicDTO userBasicDTO = new UserBasicDTO();
        userBasicDTO.setUserId(umsUser.getId());
        userBasicDTO.setNickName(umsUser.getNickName());
        userBasicDTO.setAvatar(AvatarUrlHelper.toPublicUrl(umsUser.getAvatar(), minioDomain));
        userBasicDTO.setAccountName(userInfo.getAccountName());
        // 注意：不暴露敏感信息
        return userBasicDTO;
    }

    /**
     * 批量轉換實體為DTO
     */
    public List<UserBasicDTO> toDTOList(List<UmsUser> umsUsers, List<UmsUserInfo> userInfos) {
        if (umsUsers == null) {
            return null;
        }

        Map<Long, UmsUserInfo> umsUserInfoMap = userInfos.stream().collect(Collectors.toMap(UmsUserInfo::getUserId, Function.identity()));



        return umsUsers.stream()
                .map(user->{
                    UmsUserInfo umsUserInfo = umsUserInfoMap.get(user.getId());

                    return this.toDTO(user,umsUserInfo);
                })
                .collect(Collectors.toList());
    }
}
