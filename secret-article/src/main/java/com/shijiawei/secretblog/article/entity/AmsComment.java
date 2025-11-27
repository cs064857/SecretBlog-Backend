package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * ClassName: AmsComment
 * Description:
 *
 * @Create 2025/7/16 上午2:45
 */
@TableName("ams_art_comment")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AmsComment {
    //手動設置ID,目的是與ams_comment_info做雙向鏈結
    @TableId(type = IdType.ASSIGN_ID)
    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    @TableId
    @TableField(value = "id")
    private Long id;

    ///TODO 限制為空
    @TableField(value = "comment_info_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentInfoId;
    @NotNull(message = "留言內容不可為空",groups = {Insert.class,Update.class})
    @TableField(value = "comment_content")
    private String commentContent;



}
