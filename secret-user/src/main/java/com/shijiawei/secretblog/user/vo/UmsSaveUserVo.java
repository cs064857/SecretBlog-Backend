package com.shijiawei.secretblog.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private String name;
    private String accountName;
    private String password;
    private String checkPassword;
    private LocalDate birthday;
    private Integer gender;
    private String roleId;
    private String email;
    private String address;
    private String phoneNumber;

}
