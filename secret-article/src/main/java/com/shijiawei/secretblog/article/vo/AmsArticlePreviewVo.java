package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.shijiawei.secretblog.article.entity.AmsArtTag;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: ArticlePreviewVo
 * Description:
 *
 * @Create 2025/8/3 上午2:18
 */
@Data
public class AmsArticlePreviewVo {

    /**
     * 文章標題(不可為空,最多64字符)
     */
    private String title;

    ///TODO 預覽文章時列出用戶名及用戶頭像
    private String username;
    private Long userId;
    private String avatar;
    private String accountName;

    private String categoryName;
    private Long categoryId;

    private List<AmsArtTag> amsArtTagList;

    /**
     * 創建時間
     */
    private LocalDateTime createTime;

    /**
     * 更新時間
     */
    private LocalDateTime updateTime;

    private Integer viewsCount;

    private Integer likesCount;

    private Integer bookmarksCount;


}
