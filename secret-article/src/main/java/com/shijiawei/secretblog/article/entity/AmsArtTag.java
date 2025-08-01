package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: AmsArtTag
 * Description:
 *
 * @Create 2025/7/29 上午1:41
 */
@Data
@TableName("ams_art_tag")
public class AmsArtTag {

    @NotNull(message = "主鍵ID不可為空",groups = {ValidationGroups.Delete.class,ValidationGroups.Update.class})
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "id")
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "article_id")
    private Long articleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "tags_id")
    private Long tagsId;

    @TableField(value = "create_at",fill = FieldFill.INSERT)
    private LocalDateTime createAt;
    @TableField(value = "update_at",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;

}
