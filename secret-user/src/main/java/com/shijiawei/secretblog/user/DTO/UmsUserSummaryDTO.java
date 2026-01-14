package com.shijiawei.secretblog.user.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.enumValue.Gender;
import com.shijiawei.secretblog.common.enumValue.Role;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用戶摘要資訊 DTO
 */
@Data
public class UmsUserSummaryDTO {
    /**
     * 用戶ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 暱稱
     */
    private String nickName;

    /**
     * 頭像
     */
    private String avatar;

    /**
     * 角色ID
     */
    private Role roleId;

    /**
     * 創建時間
     */
    private LocalDateTime createAt;

    /**
     * 更新時間
     */
    private LocalDateTime updateAt;

    /**
     * 生日
     */
    private String birthday;

    /**
     * 地址
     */
    private String address;

    /**
     * 性別
     */
    private Gender gender;
}
