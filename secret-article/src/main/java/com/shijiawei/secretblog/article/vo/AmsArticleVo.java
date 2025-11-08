package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ClassName: AmsArticleVo
 * Description:
 *
 * @Create 2025/7/16 上午12:56
 */
@JsonIgnoreProperties({"handler"})
@Data
public class AmsArticleVo {

    /**
     * 主鍵(雪花算法)
     */
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    private Long id;
    /**
     * 文章標題(不可為空,最多64字符)
     */
    private String title;
    /**
     * 文章內容(不可為空)
     */
    private String content;
    /**
     * 作者名稱
     */
//    private String accountName;


    private String nickName;

    private String avatar;

    /**
     * 作者名稱
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    /**
     * 文章創建時間
     */
    private LocalDateTime createTime;
    /**
     * 文章更新時間
     */
    private LocalDateTime updateTime;
    /**
     * 文章分類id
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long categoryId;
    /**
     * 文章分類名稱
     */
    private String categoryName;
    /**
     * 瀏覽量
     */
    private Integer viewsCount;
    /**
     * 喜歡量
     */
    private Integer likesCount;
    /**
     * 收藏量
     */
    private Integer bookmarksCount;
    /**
     * 評論量
     */
    private Integer commentsCount;


    private List<AmsArtTagsVo> amsArtTagsVoList;

    //    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Long tagsId;
//
//    private String tagsName;

    //
//    /**
//     * 文章ID 主鍵(雪花算法)
//     */
//
//    @JsonFormat(shape= JsonFormat.Shape.STRING)
//    private Long id;
//
//    /**
//     * 文章標題(不可為空,最多64字符)
//     */
//
//    private String title;
//
//    /**
//     * 文章內容(不可為空)
//     */
//
//    private String content;
//
//    /**
//     * 作者id(雪花算法,不可為空)
//     */
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Long userId;
//
//    /**
//     * 文章父評論的id(可為空)
//     */
//
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Long parentCommentId;
//    /**
//     * 文章分類id
//     */
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Long categoryId;
//
//    /**
//     * 文章標籤id
//     */
//
//    @JsonFormat(shape = JsonFormat.Shape.STRING)
//    private Long tagId;
//
//    /**
//     * 文章是否可顯示(0不顯示,1顯示)
//     */
//
//    private Integer deleted;
//
//    /**
//     * 創建時間
//     */
//    private LocalDateTime createTime;
//
//    /**
//     * 更新時間
//     */
//    private LocalDateTime updateTime;
//
//
//    @TableField(exist = false)
//    private static final long serialVersionUID = 1L;


}
