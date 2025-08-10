package com.shijiawei.secretblog.user.converter;

import com.shijiawei.secretblog.common.dto.UserDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用戶實體與DTO轉換器
 */
@Component
public class UserConverter {

    /**
     * 將實體轉換為DTO
     */
    public UserDTO toDTO(UmsUser umsUser) {
        if (umsUser == null) {
            return null;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setUserId(umsUser.getId());
        userDTO.setUsername(umsUser.getName());
        userDTO.setAvatar(umsUser.getAvatar());
        /// TODO 增加AccountName
//        userDTO.setAccountName(umsUser);
        // 注意：不暴露敏感信息
        return userDTO;
    }

    /**
     * 批量轉換實體為DTO
     */
    public List<UserDTO> toDTOList(List<UmsUser> umsUsers) {
        if (umsUsers == null) {
            return null;
        }

        return umsUsers.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
