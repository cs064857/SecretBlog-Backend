package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;

/**
 * ClassName: UmsStatus
 * Description: 使用者狀態表對應實體
 * 建表: secretblog_ums.ums_status
 *
 */
@Data
@Schema
@TableName("ums_status")
public class UmsStatus implements Serializable {

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
     * 最後一次的訪問時間
     */
    @TableField("last_accessed_at")
    @Schema(description = "最後一次的訪問時間")
    @NotNull(message = "最後一次的訪問時間不可為空")
    private LocalDateTime lastAccessedAt;

    /**
     * 最後一次發布文章的時間
     */
    @TableField("last_published_at")
    @Schema(description = "最後一次發布文章的時間")
    private LocalDateTime lastPublishedAt;

    private static final long serialVersionUID = 1L;

    public boolean isEmpty(){
        return ObjectUtils.allNull(id,userId,lastAccessedAt,lastPublishedAt);
    }
}

