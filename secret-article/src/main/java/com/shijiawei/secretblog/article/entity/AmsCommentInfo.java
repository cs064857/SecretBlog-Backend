package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: AmsCommentInfo
 * Description:
 *
 * @Create 2025/7/22 上午3:14
 */

@TableName("ams_comment_info")
@Data
public class AmsCommentInfo {
    ///TODO 僅設置基礎的NOT NULL
    //手動設置ID,目的是與ams_comment做雙向鏈結
    @TableId(type = IdType.ASSIGN_ID)
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "id")
    private Long id;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "article_id")
    private Long articleId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "user_id")
    private Long userId;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "comment_id")
    private Long commentId;


    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "parent_comment_id")
    private Long parentCommentId;

    @NotNull
    @TableField(value = "create_at",fill = FieldFill.INSERT)
    private LocalDateTime createAt;

    @NotNull
    @TableField(value = "update_at",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;


    @TableField(value = "nick_name")
    private String nickName;

    @TableField(value = "avatar")
    private String avatar;

}
