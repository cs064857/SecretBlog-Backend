package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shijiawei.secretblog.user.enumValue.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

/**
 * ClassName: UmsUserInfo
 * Description:
 * @Create 2024/9/14 上午4:09
 */
@Schema
@Data
@TableName(value = "ums_userInfo")
public class UmsUserInfo implements Serializable {
    /**
     * 主鍵(雪花算法,不可為空)
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description="主鍵(雪花算法,不可為空)")
    @NotNull(message = "主鍵(雪花算法,不可為空)不能為null")
    private Long id;

    /**
     * 關聯使用者(雪花算法,不可為空)
     */
    @TableField(value = "user_id")
    @Schema(description="關聯使用者(雪花算法,不可為空)")
    @NotNull(message = "關聯使用者(雪花算法,不可為空)不能為null")
    private Long userId;

    /**
     * 帳號名稱
     */
    @TableField(value = "account_name")
    @Schema(description="帳號名稱")
    @Size(max = 32,message = "帳號名稱最大長度要小於 32")
    private String accountName;

    /**
     * 密碼
     */
    @TableField(value = "`password`")
    @Schema(description="密碼")
    @Size(max = 255,message = "密碼最大長度要小於 255")
    private String password;

    /**
     * 信箱地址
     */
    @TableField(value = "email")
    @Schema(description="信箱地址")
    @Size(max = 255,message = "信箱地址最大長度要小於 255")
    private String email;

    /**
     * 生日(1970-01-01)
     */
    @TableField(value = "birthday")
    @Schema(description="生日(1970-01-01)")
    private LocalDate birthday;

    /**
     * 性別(1男性、2女性、3不願透露)
     */
    @TableField(value = "gender")
    @Schema(description="性別(1男性、2女性、3不願透露)")

    private Gender gender;

    /**
     * 居住地址
     */
    @TableField(value = "address")
    @Schema(description="居住地址")
    @Size(max = 255,message = "居住地址最大長度要小於 255")
    private String address;

    /**
     * 手機號碼
     */
    @TableField(value = "phone_number")
    @Schema(description="手機號碼")
    @Size(max = 255,message = "手機號碼最大長度要小於 255")
    private String phoneNumber;

    /**
     * 註冊時間
     */
    @TableField(value = "create_Time",fill = FieldFill.INSERT)
    @Schema(description="註冊時間")
    @NotNull(message = "註冊時間不能為null")
    private LocalDateTime createTime;

//    /**
//     * 邏輯刪除(0未刪除,1被刪除)
//     */
//    @TableField(value = "deleted")
//    @Schema(description="邏輯刪除(0未刪除,1被刪除)")
//    @NotNull(message = "邏輯刪除(0未刪除,1被刪除)不能为null")
//    private Byte deleted;

    private static final long serialVersionUID = 1L;

    public boolean isEmpty() {
        return ObjectUtils.allNull(address,phoneNumber,gender,birthday,email,password,accountName);
    }
}