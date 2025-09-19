package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: AmsArtCommentsVo
 * Description:
 *
 * @Create 2025/7/25 上午1:55
 */
@Data
public class AmsArtCommentsVo {
    private String username;
    private String commentContent;
    private Integer likesCount;
    private Integer replysCount;

    private LocalDateTime createAt;
    private LocalDateTime  updateAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentCommentId;

    private String avatar;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;
}
