package com.shijiawei.secretblog.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.user.enumValue.Gender;
import com.shijiawei.secretblog.user.enumValue.Role;
import com.shijiawei.secretblog.user.enumValue.Status;
import lombok.Data;

import java.time.LocalDate;

/**
 * ClassName: UmsSaveUserVo
 * Description:
 *
 * @Create 2024/9/14 下午4:21
 */
@Data
public class UmsSaveUserVo {
    private Status status;
    private String name;
    private String accountName;
    private String password;
    private String checkPassword;
    private LocalDate birthday;
    private Gender gender;
    private Role roleId;
    private String email;
    private String address;
    private String phoneNumber;

}
