package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章統計資料 VO
 * 用於展示從 Redis 即時獲取的統計數據
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章統計資料")
public class AmsArticleStatusVo {


    @Schema(description = "瀏覽數")
    private Integer viewsCount;

    @Schema(description = "點讚數")
    private Integer likesCount;

    @Schema(description = "收藏數")
    private Integer bookmarksCount;

    @Schema(description = "評論數")
    private Integer commentsCount;
}
