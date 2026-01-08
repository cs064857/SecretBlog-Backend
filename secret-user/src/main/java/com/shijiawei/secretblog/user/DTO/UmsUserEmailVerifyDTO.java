package com.shijiawei.secretblog.user.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ClassName: UmsUserEmailVerifyDTO
 * Description:
 *
 * @Create 2025/2/15 上午12:41
 */
@Data
public class UmsUserEmailVerifyDTO {

    @NotBlank(message = "郵箱不能為空")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "郵箱格式不正確"
    )
    private String email;

    @NotBlank(message = "圖形驗證碼不能為空")
    private String captchaCode;

    @NotBlank(message = "驗證碼標識不能為空")
    private String captchaKey;

    @Schema(description = "帳號名稱（可選；若未提供將以信箱前綴自動產生）")
    @Size(max = 255, message = "帳號名稱長度需小於等於 255")
    private String accountName;
}
