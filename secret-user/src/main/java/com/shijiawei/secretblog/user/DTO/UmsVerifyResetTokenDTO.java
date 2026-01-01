package com.shijiawei.secretblog.user.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * UmsVerifyResetTokenDTO - 驗證重設密碼 Token 請求 DTO
 */
@Data
public class UmsVerifyResetTokenDTO {

    @NotBlank(message = "Token 不能為空")
    private String token;
}
