package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import lombok.Data;

/**
* ClassName: UmsRole
* Description:
* @Create 2024/9/13 上午4:57
*/
@Schema
@Data
@TableName(value = "ums_role")
public class UmsRole implements Serializable {
    /**
     * 權限id(雪花算法,不可為空)
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description="權限id(雪花算法,不可為空)")
    @NotNull(message = "權限id(雪花算法,不可為空)不能为null")
    private Long id;

    /**
     * 權限名稱
     */
    @TableField(value = "role_name")
    @Schema(description="權限名稱")
    @Size(max = 255,message = "權限名稱最大长度要小于 255")
    private String roleName;

    /**
     * 權限等級(由權限低到權限高,1為普通用戶)
     */
    @TableField(value = "role_level")
    @Schema(description="權限等級(由權限低到權限高,1為普通用戶)")
    @NotNull(message = "權限等級(由權限低到權限高,1為普通用戶)不能为null")
    private Byte roleLevel;

    /**
     * 是否啟用(0為啟用,1為不啟用)
     */
    @TableField(value = "deleted")
    @Schema(description="是否啟用(0為啟用,1為不啟用)")
    @NotNull(message = "是否啟用(0為啟用,1為不啟用)不能为null")
    private Byte deleted;

    private static final long serialVersionUID = 1L;
}