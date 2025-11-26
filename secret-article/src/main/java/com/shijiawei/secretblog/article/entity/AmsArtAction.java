package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ClassName: AmsArtAction
 * Description:
 *
 * @Create 2025/11/25 下午10:00
 */

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@TableName(value = "ams_art_action")
public class AmsArtAction implements Serializable {
    @Serial
    private static final long serialVersionUID = 6115612611515648248L;

    @Schema(description = "主鍵(雪花算法,不可為空)")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    @JsonFormat(shape = JsonFormat.Shape.STRING)

    private Long id;

    @Schema(description = "文章id")
    @TableField("article_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    @Schema(description = "用戶id")
    @TableField("user_id")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    /**
     * 是否點讚 (0:未點讚, 1:已點讚)
     * 建議：雖資料庫為 tinyint，但業務邏輯可映射為 Boolean 或 Integer
     */
    @Schema(description = "是否點讚 (0:未點讚, 1:已點讚)")
    @TableField("is_liked")
    private Byte isLiked;

    @Schema(description = "是否已收藏 (0:未收藏, 1:已收藏)")
    @TableField("is_bookmarked")
    private Byte isBookmarked;

    @Schema(description = "首次互動時間")
    @TableField(value = "create_at", fill = FieldFill.INSERT)
    private LocalDateTime createAt;

    @Schema(description = "最後更新互動狀態時間")
    @TableField(value = "update_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;

}
