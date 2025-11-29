package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * ClassName: UserLikedArticleVo
 * Description: 用戶點讚文章資訊
 *
 * @Create 2025/11/29
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Schema(description = "用戶點讚文章資訊")
public class UserLikedArticleVo {

    @Schema(description = "文章ID")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    @Schema(description = "文章標題")
    private String title;

    @Schema(description = "是否點讚")
    private Byte isLiked;

    @Schema(description = "是否收藏")
    private Byte isBookmarked;

    @Schema(description = "最後互動時間")
    private LocalDateTime updateAt;
}
