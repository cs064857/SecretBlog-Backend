package com.shijiawei.secretblog.article.vo;

import lombok.Data;

/**
 * ClassName: AmsSaveArticleVo
 * Description:編輯器中新增文章
 *
 * @Create 2024/8/30 上午3:14
 */
@Data
public class AmsSaveArticleVo {
    private String title;
    private String content;
    private Long categoryId;
}
