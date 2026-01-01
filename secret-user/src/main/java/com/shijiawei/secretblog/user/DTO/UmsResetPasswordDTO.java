package com.shijiawei.secretblog.user.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * UmsResetPasswordDTO - 重設密碼請求 DTO
 * 用於使用 Token 重設密碼
 */
@Data
public class UmsResetPasswordDTO {

    @NotBlank(message = "Token 不能為空")
    private String token;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, max = 32, message = "密碼長度需在 6-32 字元之間")
    private String newPassword;

    @NotBlank(message = "確認密碼不能為空")
    private String confirmPassword;
}
