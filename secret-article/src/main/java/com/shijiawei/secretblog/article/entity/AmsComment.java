package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: AmsComment
 * Description:
 *
 * @Create 2025/7/16 上午2:45
 */
@TableName("ams_artComment")
@Data
public class AmsComment {

    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId
    @TableField(value = "id")
    private Long id;

    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
    @TableField(value = "article_id")
    private Long articleId;
    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "parent_comment_id")
    private Long parent_commentId;
    ///TODO 限制為空
    @TableField(value = "comment_info_id")
    private Long commentInfoId;
    @NotNull(message = "評論內容不可為空",groups = {Insert.class,Update.class})
    @TableField(value = "comment_content")
    private String commentContent;


    @TableField(value = "create_at",fill = FieldFill.INSERT)
    private LocalDateTime createAt;

    @TableField(value = "update_at",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;


}
