package com.shijiawei.secretblog.user.DTO;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
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

    @TableField(value = "account_name")
    @Schema(description="帳號名稱")
    @Size(max = 32,message = "帳號名稱最大長度要小於 32")
    private String accountName;
}
