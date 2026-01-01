package com.shijiawei.secretblog.user.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * UmsForgotPasswordDTO - 忘記密碼請求 DTO
 * 用於請求發送密碼重設驗證碼
 */
@Data
public class UmsForgotPasswordDTO {

    @NotBlank(message = "郵箱不能為空")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "郵箱格式不正確"
    )
    private String email;
}
