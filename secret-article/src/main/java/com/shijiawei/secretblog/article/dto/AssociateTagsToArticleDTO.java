package com.shijiawei.secretblog.article.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AssociateTagsToArticleDTO
 * Description:
 *
 * @Create 2025/7/29 上午2:03
 */
@Data
public class AssociateTagsToArticleDTO {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("article_id")
    private Long articleId;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @JsonProperty("tags_id")
    private List<Long> tagsId;
}
