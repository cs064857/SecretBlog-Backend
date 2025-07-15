package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: AmsCommentCreateDTO
 * Description:
 *
 * @Create 2025/7/16 上午4:18
 */
@Data
public class AmsCommentCreateDTO {

    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
    private Long articleId;
    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
    private Long userId;

    @NotNull(message = "評論內容不可為空",groups = {Insert.class,Update.class})
    private String commentContent;


}
