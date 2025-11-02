package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: AmsArtCommentStaticVo
 * Description:放置評論的快取靜態資訊，用於快取的動靜分離
 *
 * @Create 2025/10/30 下午11:12
 */
@Data
public class AmsArtCommentStaticVo {
    private String username;
    private String commentContent;

    private LocalDateTime createAt;
    private LocalDateTime  updateAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long parentCommentId;

    private String avatar;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;
}
