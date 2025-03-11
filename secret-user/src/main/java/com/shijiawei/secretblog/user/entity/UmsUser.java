package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.shijiawei.secretblog.user.enumValue.Role;
import com.shijiawei.secretblog.user.enumValue.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
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
     * 姓名
     */
    @TableField(value = "`name`")
    @Schema(description="姓名")
    @Size(max = 255,message = "姓名最大长度要小于 255")
    private String name;

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



    private static final long serialVersionUID = 1L;

    public boolean isEmpty() {
        return ObjectUtils.allNull(name, avatar, status, roleId);
    }
}