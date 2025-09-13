package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.article.entity.AmsCategory;
import lombok.Data;

import java.util.List;

/**
 * ClassName: ArticleCategoryVo
 * Description:
 *
 * @Create 2024/8/28 下午6:03
 */
@Data
public class AmsCategoryTreeVo {
    /**
     * 分類ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;
    /**
     * 分類名稱
     */
    private String label;
    /**
     * 分類子類
     */
    private List<AmsCategoryTreeVo> children;
}
