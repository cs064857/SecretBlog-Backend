package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: AmsCommentActionVo
 * Description: 留言互動狀態 VO
 *
 * @Create 2025/11/26
 */

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AmsCommentActionVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long commentId;

    /**
     * 是否點讚 (0:未點讚, 1:已點讚)
     */
    private Byte isLiked;

    /**
     * 是否收藏 (0:未收藏, 1:已收藏)
     */
    private Byte isBookmarked;

}
