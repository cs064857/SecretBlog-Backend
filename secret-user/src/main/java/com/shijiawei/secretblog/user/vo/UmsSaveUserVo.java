package com.shijiawei.secretblog.user.vo;

import com.shijiawei.secretblog.common.enumValue.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * ClassName: UmsSaveUserVo
 * Description: 管理員新增用戶請求VO
 *
 * @Create 2024/9/14 下午4:21
 */
@Data
public class UmsSaveUserVo {
    
    @NotBlank(message = "帳號不能為空")
    @Size(min = 6, max = 30, message = "帳號長度必須介於6到30個字元之間")
    private String accountName;
    
    //使用者暱稱(可選，若未提供則自動產生)
    @Size(max = 10, message = "使用者名稱長度不能超過10個字元")
    private String nickName;
    
    @NotBlank(message = "信箱不能為空")
    @Email(message = "請輸入有效的電子郵件格式")
    private String email;
    
    @NotBlank(message = "密碼不能為空")
    @Size(min = 8, max = 30, message = "密碼長度必須介於8到30個字元之間")
    private String password;
    
    @NotBlank(message = "確認密碼不能為空")
    private String checkPassword;
    
    //出生年月日(可選)
    private LocalDate birthday;
    
    //性別(可選)
    private Gender gender;
    
    //地址(可選)
    private String address;
    
    //手機號碼(可選)
    @Pattern(regexp = "^$|^09\\d{8}$", message = "請輸入有效的手機號碼，例如 0912345678")
    private String phoneNumber;
}
