package com.shijiawei.secretblog.user.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.user.enumValue.Gender;
import com.shijiawei.secretblog.user.enumValue.Role;
import com.shijiawei.secretblog.user.enumValue.Status;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Blob;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: UmsSaveUserVo
 * Description:
 *
 * @Create 2024/9/14 下午4:21
 */
@Data
public class UmsSaveUserVo {
//    private Object avatar;
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

    // ID相關
//    private String id;  // 或 Long id;
//    private String userInfoId;  // 或 Long userInfoId;
//
//    // 基本資料
//    private String name;
//    private MultipartFile avatar;  // 若是文件上傳用
//    private Status status;  // 或使用枚舉 enum Status { Normal, Disabled }
//    private String accountName;
//    private String password;
//    private String checkPassword;
//    private String email;
//
//    // 時間相關
//    @DateTimeFormat(pattern = "yyyy-MM-dd")
//    private LocalDate birthday;
//
//    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
//    private LocalDateTime createTime;
//
//    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
//    private LocalDateTime foo;
//
//    // 其他個人資料
//    private Gender gender;  // 或使用枚舉 enum Gender { male, female }
//    private String address;
//    private String phoneNumber;
//    private Role roleId;  // 或使用枚舉 enum Role { 管理員, 一般使用者 }

}
