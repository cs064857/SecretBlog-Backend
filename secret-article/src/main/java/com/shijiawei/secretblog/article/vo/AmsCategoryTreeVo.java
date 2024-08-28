package com.shijiawei.secretblog.article.vo;

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
    private Long id;
    private String label;
    private List<AmsCategoryTreeVo> children;
}
