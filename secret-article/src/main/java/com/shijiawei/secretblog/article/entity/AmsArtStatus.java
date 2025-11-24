package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: AmsArtStatus
 * Description:
 *
 * @Create 2025/8/3 上午2:32
 */
@Data
@TableName("ams_art_status")
public class AmsArtStatus {

    @TableField(value = "id")
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    @NotNull(message = "文章ID不能為空",groups = {ValidationGroups.Insert.class})
    @TableField(value = "article_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;
    @Min(value = 0, message = "瀏覽數不能為負數")
    @TableField(value = "views_count")
    private Integer viewsCount;
    @Min(value = 0, message = "點讚數不能為負數")
    @TableField(value = "likes_count")
    private Integer likesCount;
    @Min(value = 0, message = "收藏數不能為負數")
    @TableField(value = "bookmarks_count")
    private Integer bookmarksCount;

    @Min(value = 0, message = "留言數不能為負數")
    @TableField(value = "comments_count")
    private Integer commentsCount;


}
