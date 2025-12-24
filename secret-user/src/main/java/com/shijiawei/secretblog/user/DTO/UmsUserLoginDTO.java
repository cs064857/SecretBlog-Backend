package com.shijiawei.secretblog.user.DTO;

import com.shijiawei.secretblog.common.enumValue.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用戶登入 dto")
public class UmsUserLoginDTO {

    @Schema(description = "帳號名稱")
    private String accountName;

    @Schema(description = "密碼")
    private String password;

    @Schema(description = "信箱地址")
    private String email;

    @Schema(description = "使用者組Id")
    private Role roleId;

    @Schema(description = "邏輯刪除(0未刪除,1被刪除)")
    private Byte deleted;

    @Schema(description = "用戶ID")
    private Long userId;

    @Schema(description = "用戶頭像")
    private String avatar;
}

