package com.shijiawei.secretblog.user.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UmsChangePasswordDTO - 修改密碼請求 DTO
 * 用於已登入用戶修改密碼
 */
@Data
public class UmsChangePasswordDTO {

    @NotBlank(message = "舊密碼不能為空")
    private String oldPassword;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, max = 32, message = "密碼長度需在 6-32 字元之間")
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;
}
