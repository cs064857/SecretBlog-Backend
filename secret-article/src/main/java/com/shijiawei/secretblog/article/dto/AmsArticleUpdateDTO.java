package com.shijiawei.secretblog.article.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AmsArticleUpdateDTO
 * Description:
 *
 * @Create 2025/10/21 上午12:39
 */
@Data
public class AmsArticleUpdateDTO {

    /**
     * 文章ID
     */
//    @JsonFormat(shape= JsonFormat.Shape.STRING)
//    @NotBlank(message = "文章ID不可為空",groups = {ValidationGroups.Update.class})
//    private Long articleId;
    /**
     * 文章標題(不可為空,最多64字符)
     */
    @NotBlank(message = "文章標題不可為空",groups = {ValidationGroups.Update.class})
    private String title;
    /**
     * 文章內容(不可為空)
     */
    @NotBlank(message = "文章內容不可為空",groups = {ValidationGroups.Update.class})
    private String content;

//
//    /**
//     * 文章更新時間
//     */
//    private LocalDateTime updateTime;
    /**
     * 文章分類id
     */
    @NotBlank(message = "文章分類ID不可為空",groups = {ValidationGroups.Update.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;
//    /**
//     * 文章分類名稱
//     */
//    private String categoryName;

    @NotBlank(message = "標籤不可為空",groups = {ValidationGroups.Update.class})
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<Long> tagsId;
}
