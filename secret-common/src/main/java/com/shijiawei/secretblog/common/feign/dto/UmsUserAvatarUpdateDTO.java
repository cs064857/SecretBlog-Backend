package com.shijiawei.secretblog.common.feign.dto;

import lombok.Data;

/**
 * ClassName: UmsUserAvatarUpdateDTO
 * Description: DTO for updating user avatar via Feign
 */
@Data
public class UmsUserAvatarUpdateDTO {
    private Long userId;
    private String avatar;

    public UmsUserAvatarUpdateDTO() {
    }

    public UmsUserAvatarUpdateDTO(Long userId, String avatar) {
        this.userId = userId;
        this.avatar = avatar;
    }
}
