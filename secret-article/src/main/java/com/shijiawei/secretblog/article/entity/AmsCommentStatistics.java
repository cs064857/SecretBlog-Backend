package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * ClassName: AmsCommentStatistics
 * Description:
 *
 * @Create 2025/11/7 下午10:27
 */
@TableName("ams_comment_statistics")
@Data
public class AmsCommentStatistics {
    @TableId(type = IdType.ASSIGN_ID)
    @TableField(value = "id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @TableField(value = "article_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    @TableField(value = "comment_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;

    @TableField(value = "likes_count")
    private Integer likesCount;

    @TableField(value = "replies_count")
    private Integer repliesCount;
}
