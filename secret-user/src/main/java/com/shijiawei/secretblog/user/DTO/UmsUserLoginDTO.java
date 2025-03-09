package com.shijiawei.secretblog.user.DTO;

import lombok.Data;

/**
 * ClassName: UmsUserLoginDTO
 * Description:
 *
 * @Create 2025/3/2 上午3:48
 */
@Data
public class UmsUserLoginDTO {
    ///TODO JSR303
    private String accountName;
    private String email;
    private String password;


}
