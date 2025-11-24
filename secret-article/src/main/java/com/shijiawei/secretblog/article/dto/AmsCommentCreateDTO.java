package com.shijiawei.secretblog.article.dto;

import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.shijiawei.secretblog.common.vaildation.Insert;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: AmsCommentCreateDTO
 * Description:
 *
 * @Create 2025/7/16 上午4:18
 */
@Data
public class AmsCommentCreateDTO {

//    @NotNull(message = "主鍵不可為空",groups = {Insert.class,Update.class})
//    private Long articleId;

    @NotNull(message = "留言內容不可為空",groups = {Insert.class,Update.class})
    private String commentContent;

    //可為空
    private Long parentCommentId;
}
