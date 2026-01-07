package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: UmsAuths
 * Description: 使用者認證資訊表對應實體
 * 建表: secretblog_ums.ums_auths
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema
@TableName("ums_auths")
public class UmsAuths implements Serializable {

    /**
     * 主鍵ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @Schema(description = "主鍵ID")
    @NotNull(message = "主鍵ID不可為空")
    private Long id;

    /**
     * 用戶ID
     */
    @TableField("user_id")
    @Schema(description = "用戶ID")
    @NotNull(message = "用戶ID不可為空")
    private Long userId;

    /**
     * 密碼
     */
    @TableField("password")
    @Schema(description = "密碼")
    @Size(max = 255, message = "密碼長度需小於等於 255")
    private String password;

    /**
     * 密碼更新時間
     */
    @TableField("password_updated_at")
    @Schema(description = "密碼更新時間")
    @NotNull(message = "密碼更新時間不可為空")
    private LocalDateTime passwordUpdatedAt;

    /**
     * 創建時間
     */
    @TableField("create_at")
    @Schema(description = "創建時間")
    @NotNull(message = "創建時間不可為空")
    private LocalDateTime createAt;

    /**
     * 更新時間
     */
    @TableField("update_at")
    @Schema(description = "更新時間")
    @NotNull(message = "更新時間不可為空")
    private LocalDateTime updateAt;

    private static final long serialVersionUID = 1L;
}

