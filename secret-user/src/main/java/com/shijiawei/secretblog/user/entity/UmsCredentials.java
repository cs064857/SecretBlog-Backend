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
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

/**
 * ClassName: UmsCredentials
 * Description: 使用者憑證資訊表對應實體
 * 建表: secretblog_ums.ums_credentials
 *
 * 註解均採用繁體中文
 */
@Data
@Schema
@TableName("ums_credentials")
public class UmsCredentials implements Serializable {

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
     * 電子郵件地址
     */
    @TableField("email")
    @Schema(description = "電子郵件地址")
    @Size(max = 255, message = "電子郵件長度需小於等於 255")
    private String email;

    /**
     * 電話號碼
     */
    @TableField("phone_number")
    @Schema(description = "電話號碼")
    @Size(max = 255, message = "電話號碼長度需小於等於 255")
    private String phoneNumber;

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

    public boolean isEmpty(){
        return ObjectUtils.allNull(email,phoneNumber,createAt,updateAt);
    }
}

