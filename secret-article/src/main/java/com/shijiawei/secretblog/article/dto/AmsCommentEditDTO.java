package com.shijiawei.secretblog.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: AmsCommentEditDTO
 * Description: 用於編輯留言的 DTO
 *
 * @author 
 * @date 2025/11/28
 */
@Data
public class AmsCommentEditDTO {

    @NotNull(message = "留言ID不可為空")
    private Long commentId;

    @NotBlank(message = "留言內容不可為空")
    private String commentContent;
}

