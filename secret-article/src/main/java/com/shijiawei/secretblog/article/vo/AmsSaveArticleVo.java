package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AmsSaveArticleVo
 * Description:編輯器中新增文章
 *
 * @Create 2024/8/30 上午3:14
 */
@Data
public class AmsSaveArticleVo {
    /**
     * 文章標題(不可為空,最多64字符)
     */
    @NotBlank(message = "新增時文章標題不可為空",groups = {Insert.class})
    private String title;

    /**
     * 文章內容(不可為空)
     */
    @NotBlank(message = "新增時文章內容不可為空",groups = {Insert.class})
    private String content;

    /**
     * 文章分類id
     */
    @NotNull(message = "文章分類ID不可為空",groups = {Update.class,Insert.class})
    private Long categoryId;

    private String jwtToken;

    private List<Long> tagsId;

}
