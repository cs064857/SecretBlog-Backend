package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: AmsUserCommentVo
 * Description: 用戶留言列表輸出模型
 *
 * @Create 2025/1/19 下午
 */
@Data
public class AmsUserCommentVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;

    private String commentContent;

    private String articleTitle;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;
}
