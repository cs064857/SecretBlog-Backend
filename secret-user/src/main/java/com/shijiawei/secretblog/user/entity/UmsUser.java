package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shijiawei.secretblog.common.enumValue.Role;
import com.shijiawei.secretblog.common.enumValue.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

/**
* ClassName: UmsUser
* Description:
* @Create 2024/9/14 上午3:57
*/
@Schema
@Data
@TableName(value = "ums_user")
public class UmsUser implements Serializable {

    /**
     * 主鍵(雪花算法,不可為空)
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description="主鍵(雪花算法,不可為空)")
    @NotNull(message = "主鍵(雪花算法,不可為空)不能为null")
    private Long id;

    /**
     * 關聯使用者資訊(雪花算法,不可為空)
     */
    @TableField(value = "userInfo_id")
    @Schema(description="關聯使用者資訊(雪花算法,不可為空)")
    @NotNull(message = "關聯使用者資訊(雪花算法,不可為空)不能为null")
    private Long userinfoId;

    /**
     * 用戶名
     * DDL變更: 原 name 欄位更名為 nick_name
     */
    @TableField(value = "nick_name")
    @Schema(description="用戶名")
    @Size(max = 255,message = "用戶名最大长度要小于 255")
    private String nickName;

//    /**
//     * 用戶帳號
//     */
//    @TableField(value = "account_name")
//    @Schema(description="帳號")
//    @Size(max = 255,message = "用戶帳號最大长度要小于 255")
//    private String accountName;


    /**
     * 使用者組Id
     */
    @TableField(value = "role_id")
    @Schema(description="使用者組Id")
    @NotNull(message = "使用者組Id不能为null")
    private Role roleId;

    /**
     * 使用者頭像
     */
    @TableField(value = "avatar")
    @Schema(description="使用者頭像")
    @Size(max = 2083,message = "使用者頭像最大长度要小于 2083")
    private String avatar;


    @TableField(value = "status")
    @Schema(description="帳號狀態(0正常,1封禁中)")
    @NotNull(message = "帳號狀態(0正常,1封禁中)不能为null")
    private Status status;



    /**
     * 邏輯刪除(0未刪除,1被刪除)
     */
    @TableField(value = "deleted")
    @Schema(description="邏輯刪除(0未刪除,1被刪除)")
    @NotNull(message = "邏輯刪除(0未刪除,1被刪除)不能为null")
    private Byte deleted;



    /**
     * 創建時間
     */
    @TableField(value = "create_at")
    @Schema(description="創建時間")
    private LocalDateTime createAt;

    /**
     * 更新時間
     */
    @TableField(value = "update_at")
    @Schema(description="更新時間")
    private LocalDateTime updateAt;

    private static final long serialVersionUID = 1L;

    public boolean isEmpty() {
        return ObjectUtils.allNull(nickName, avatar, status, roleId);
    }
}