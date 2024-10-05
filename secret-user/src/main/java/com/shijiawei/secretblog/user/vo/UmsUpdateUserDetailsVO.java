package com.shijiawei.secretblog.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.user.enumValue.Gender;
import com.shijiawei.secretblog.user.enumValue.Role;
import com.shijiawei.secretblog.user.enumValue.Status;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: UmsUpdateUserDetailsVO
 * Description:
 *
 * @Create 2024/9/21 下午5:51
 */
@Data
public class UmsUpdateUserDetailsVO {
    // 姓名
    private String name;

    // 使用者頭像
    private String avatar;

    //帳號狀態(0正常,1封禁中)
    private Status status;

    // UmsUserInfo 的欄位
    private String accountName;    // 帳號名稱
    private String password;       // 密碼
    private String email;          // 信箱地址
    private LocalDate birthday;    // 生日 (LocalDate in Java)
    private Gender gender;            // 性別 (1 男性, 2 女性, 3 不願透露)
    private String address;        // 居住地址
    private String phoneNumber;    // 手機號碼

    // 來自 UmsRole 的欄位
    private Role roleId;       // 權限名稱

    public boolean isEmpty() {
        return ObjectUtils.allNull(
                name, avatar, status, accountName, password, email, birthday, gender,
                address, phoneNumber,roleId
        );
    }
}
