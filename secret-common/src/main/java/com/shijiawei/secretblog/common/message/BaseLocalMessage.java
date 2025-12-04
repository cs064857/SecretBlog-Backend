package com.shijiawei.secretblog.common.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.shijiawei.secretblog.common.codeEnum.LocalMessage;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 本地消息表抽象父類別
 * 子類別只需繼承並添加 @TableName 註解
 */
@Data
public abstract class BaseLocalMessage implements Serializable, LocalMessage {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID(自增，確保發送順序)
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主鍵ID(自增，確保發送順序)")
    private Long id;

    /**
     * 業務消息唯一標識(UUID或業務ID，用於冪等校驗)
     */
    @TableField("msg_id")
    @Schema(description = "業務消息唯一標識")
    @NotBlank(message = "業務消息唯一標識不可為空")
    @Size(max = 64, message = "業務消息唯一標識長度需小於等於64字元")
    private String msgId;

    /**
     * RabbitMQ Exchange名稱
     */
    @TableField("exchange")
    @Schema(description = "RabbitMQ Exchange名稱")
    @NotBlank(message = "Exchange名稱不可為空")
    @Size(max = 100, message = "Exchange名稱長度需小於等於100字元")
    private String exchange;

    /**
     * RabbitMQ Routing Key
     */
    @TableField("routing_key")
    @Schema(description = "RabbitMQ Routing Key")
    @NotBlank(message = "Routing Key不可為空")
    @Size(max = 100, message = "Routing Key長度需小於等於100字元")
    private String routingKey;

    /**
     * 消息內容(JSON格式)
     */
    @TableField("content")
    @Schema(description = "消息內容(JSON格式)")
    @NotBlank(message = "消息內容不可為空")
    private String content;

    /**
     * 狀態: 0-待發送, 1-已發送, 2-發送失敗
     */
    @TableField("status")
    @Schema(description = "狀態: 0-待發送, 1-已發送, 2-發送失敗")
    @NotNull(message = "消息狀態不可為空")
    private Integer status;

    /**
     * 重試次數
     */
    @TableField("retry_count")
    @Schema(description = "重試次數")
    @NotNull(message = "重試次數不可為空")
    private Integer retryCount;

    /**
     * 下一次重試時間(用於指數退避)
     */
    @TableField("next_retry_at")
    @Schema(description = "下一次重試時間(用於指數退避)")
    @NotNull(message = "下一次重試時間不可為空")
    private LocalDateTime nextRetryAt;

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

    /**
     * 最後一次失敗的錯誤訊息
     */
    @TableField("error_msg")
    @Schema(description = "最後一次失敗的錯誤訊息")
    private String errorMsg;
}
