package com.shijiawei.secretblog.article.vo;

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
    private AmsArtCommentsVo parentComment;
    private String avatar;
    private Long commentId;
}
