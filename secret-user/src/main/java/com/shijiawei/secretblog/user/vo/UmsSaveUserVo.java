package com.shijiawei.secretblog.user.vo;

import com.shijiawei.secretblog.common.enumValue.Gender;
import com.shijiawei.secretblog.common.enumValue.Role;
import com.shijiawei.secretblog.common.enumValue.Status;
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
//    private Object avatar;
    private Status status;
    // DDL變更: 原 name 欄位更名為 nickName
    private String nickName;
    private String accountName;
    ///TODO password 欄位已從資料庫移除，如需保留請手動添加欄位
    private String password;
    private String checkPassword;
    private LocalDate birthday;
    private Gender gender;
    private Role roleId;
    ///TODO email 欄位已從資料庫移除，如需保留請手動添加欄位
    private String email;
    private String address;
    ///TODO phoneNumber 欄位已從資料庫移除，如需保留請手動添加欄位
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
