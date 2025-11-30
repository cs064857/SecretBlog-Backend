package com.shijiawei.secretblog.common.feign.dto;

import lombok.Data;

/**
 * ClassName: AmsAuthorAvatarUpdateDto
 * Description:
 *
 * @Create 2025/11/30 下午10:58
 */
@Data
public class AmsAuthorAvatarUpdateDTO {
    private Long userId;
    private String avatar;

    public AmsAuthorAvatarUpdateDTO(Long userId, String avatar) {
        this.userId = userId;

        this.avatar = avatar;
    }
}