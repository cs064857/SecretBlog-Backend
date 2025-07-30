package com.shijiawei.secretblog.article.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    private Long article_id;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tags_id;
}
