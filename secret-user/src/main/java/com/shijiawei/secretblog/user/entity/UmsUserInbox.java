package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.nimbusds.oauth2.sdk.util.JSONArrayUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 使用者通知收件匣。
 */
@Schema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ums_user_inbox")
public class UmsUserInbox implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主鍵ID(自增）
     */
    @TableId(value = "id", type = IdType.AUTO)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "主鍵ID(自增）")
    private Long id;

    /**
     * 收件者使用者ID
     */
    @TableField("to_user_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "收件者使用者ID")
    @NotNull(message = "收件者使用者ID不可為空")
    private Long toUserId;

    /**
     * 觸發者使用者ID(可為空）
     */
    @TableField("from_user_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "觸發者使用者ID(可為空）")
    private Long fromUserId;

    /**
     * 觸發者頭像(可為空）
     */
    @TableField("from_avatar")
    @Schema(description = "觸發者頭像(可為空）")
    @Size(max = 2083, message = "觸發者頭像長度需小於等於 2083 字元")
    private String fromAvatar;

    /**
     * 觸發者暱稱(可為空）
     */
    @TableField("from_nick_name")
    @Schema(description = "觸發者暱稱(可為空）")
    @Size(max = 255, message = "觸發者暱稱長度需小於等於 255 字元")
    private String fromNickName;

    /**
     * 通知類型(例如：COMMENT_REPLIED、ARTICLE_REPLIED）
     */
    @TableField("type")
    @Schema(description = "通知類型")
    @NotBlank(message = "通知類型不可為空")
    @Size(max = 50, message = "通知類型長度需小於等於50字元")
    private String type;

    /**
     * 通知標題(例如文章標題，可為空）
     */
    @TableField("subject")
    @Schema(description = "通知標題(可為空）")
    @Size(max = 255, message = "通知標題長度需小於等於255字元")
    private String subject;

    /**
     * 通知內容(可為空）
     */
    @TableField("body")
    @Schema(description = "通知內容(可為空）")
    private String body;

    /**
     * 關聯文章ID(可為空）
     */
    @TableField("article_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Schema(description = "關聯文章ID(可為空）")
    private Long articleId;

    /**
     * 是否已讀：0 未讀、1 已讀
     */
    @TableField("read_flag")
    @Schema(description = "是否已讀：0 未讀、1 已讀")
    @NotNull(message = "是否已讀不可為空")
    private Integer readFlag;

    /**
     * 已讀時間(read_flag=1 時）
     */
    @TableField("read_at")
    @Schema(description = "已讀時間(read_flag=1 時）")
    private LocalDateTime readAt;

    /**
     * 邏輯刪除：0 未刪除、1 已刪除
     */
    @TableField("deleted")
    @Schema(description = "邏輯刪除：0 未刪除、1 已刪除")
    @NotNull(message = "邏輯刪除不可為空")
    private Integer deleted;

    /**
     * 建立時間
     */
    @TableField("create_at")
    @Schema(description = "建立時間")
    @NotNull(message = "建立時間不可為空")
    private LocalDateTime createAt;

    /**
     * 更新時間
     */
    @TableField("update_at")
    @Schema(description = "更新時間")
    @NotNull(message = "更新時間不可為空")
    private LocalDateTime updateAt;
}
