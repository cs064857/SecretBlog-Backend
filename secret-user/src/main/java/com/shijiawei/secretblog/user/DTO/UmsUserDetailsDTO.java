package com.shijiawei.secretblog.user.DTO;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: UmsUserDetailsDTO
 * Description:
 *
 * @Create 2024/9/15 上午1:43
 */
@Data
public class UmsUserDetailsDTO {

    // 姓名
    private String name;

    // 使用者頭像
    private String avatar;

    //帳號狀態(0正常,1封禁中)
    private int status;
    // 邏輯刪除 (0 未刪除, 1 已刪除)
    private int deleted;

    // UmsUserInfo 的欄位
    private String accountName;    // 帳號名稱
    private String password;       // 密碼
    private String email;          // 信箱地址
    private LocalDate birthday;    // 生日 (LocalDate in Java)
    private Integer gender;            // 性別 (1 男性, 2 女性, 3 不願透露)
    private String address;        // 居住地址
    private String phoneNumber;    // 手機號碼
    private LocalDateTime createTime;  // 註冊時間 (LocalDateTime in Java)

    // 來自 UmsRole 的欄位
    private String roleName;       // 權限名稱
}
