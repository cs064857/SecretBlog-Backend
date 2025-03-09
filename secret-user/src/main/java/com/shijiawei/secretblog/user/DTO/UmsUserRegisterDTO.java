package com.shijiawei.secretblog.user.DTO;

import java.time.LocalDate;

import com.shijiawei.secretblog.user.enumValue.Gender;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UmsUserRegisterDTO {

    @NotBlank(message = "用戶名不能為空")
    @Pattern(regexp = "^[A-Za-z0-9]{4,16}$",message = "用戶名只能包含英文和數字，長度在4-16之間")
    private String name;

    @NotBlank(message = "帳號不能為空")
    @Pattern(regexp = "^[A-Za-z0-9]{4,16}$",message = "帳號只能包含英文和數字，長度在4-16之間")
    private String accountName;


    @NotBlank(message = "密碼不能為空")
    @Pattern(regexp = "^[A-Za-z0-9~!@#\\\\$%^&*()_+`={}\\\\[\\\\]:\\\";'<>?,./-]{4,16}$",message = "密碼只能包含英文和數字與常見特殊符號，長度在4-16之間")
    private String password;

    @NotBlank(message = "確認密碼不能為空")
    @Pattern(regexp = "^[A-Za-z0-9~!@#\\\\$%^&*()_+`={}\\\\[\\\\]:\\\";'<>?,./-]{4,16}$",message = "確認密碼只能包含英文和數字與常見特殊符號，長度在4-16之間")
    private String checkPassword;


    @NotBlank(message = "郵箱不能為空")
    @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "郵箱格式不正確"
    )
    private String email;

//    @NotNull(message = "生日不能為空")
//    @Pattern(regexp="^\\\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\\\d|3[01])$",message = "生日格式不正確")
//    private LocalDate birthday;

    @NotNull(message = "性別不能為空")
//    @Min(value = 1, message = "性別格式不正確")
//    @Max(value = 3, message = "性別格式不正確")
    private Gender gender;

    @NotBlank(message = "驗證碼不能為空")
    private String emailValidCode;



}
