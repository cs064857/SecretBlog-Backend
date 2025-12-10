package com.shijiawei.secretblog.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章預覽共享 DTO
 * 用於 secret-search 透過 Feign 調用 secret-article 時傳輸文章預覽資料
 * 包含建構 Elasticsearch ArticlePreviewDocument 所需的所有欄位
 */
@Data
public class ArticlePreviewDTO {

    /**
     * 文章ID（雪花算法）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    /**
     * 用戶ID（雪花算法）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 文章作者暱稱
     */
    private String nickName;

    /**
     * 文章標題
     */
    private String title;

    /**
     * 文章內容（原始 Markdown 或 HTML 格式）
     */
    private String content;

    /**
     * 文章分類ID（雪花算法）
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    /**
     * 文章分類名稱
     */
    private String categoryName;

    /**
     * 文章標籤列表
     */
    private List<AmsArtTagsDTO> amsArtTagList;

    /**
     * 文章創建時間
     */
    private LocalDateTime createTime;

    /**
     * 文章更新時間
     */
    private LocalDateTime updateTime;
}
