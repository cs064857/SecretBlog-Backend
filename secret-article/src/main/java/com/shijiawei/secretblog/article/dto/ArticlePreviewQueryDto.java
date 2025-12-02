package com.shijiawei.secretblog.article.dto;

import lombok.Data;
import lombok.Getter;

import java.util.List;

/**
 * ClassName: ArticlePreviewQueryDto
 * Description:
 *
 * @Create 2025/12/2 下午4:25
 */
@Getter
public class ArticlePreviewQueryDto {

    //

    private Integer routePage;

    private final Integer pageSize=20;

    private Long categoryId;

    private List<Long> tagsId;



}
