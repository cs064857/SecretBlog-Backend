package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.List;

/**
 * 文章編輯用VO
 * 用於提供前端編輯器所需資料，其中content為原始Markdown內容。
 */
@Data
public class AmsArticleEditVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String title;

    /**
     * 原始內容(Markdown)
     */
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tagsId;
}

