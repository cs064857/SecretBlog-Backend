package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.article.entity.AmsArticle;

import java.time.LocalDateTime;

/**
 * ClassName: AmsArticleVo
 * Description:
 *
 * @Create 2025/7/16 上午12:56
 */
public class AmsArticleVo {

    /**
     * 文章ID 主鍵(雪花算法)
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
     * 作者id(雪花算法,不可為空)
     */

    private Long userId;

    /**
     * 文章分類id
     */

    private Long categoryId;

    /**
     * 文章標籤id
     */

    private Long tagId;

    /**
     * 文章是否可顯示(0不顯示,1顯示)
     */

    private Integer deleted;

    /**
     * 創建時間
     */
    private LocalDateTime createTime;

    /**
     * 更新時間
     */
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private static final long serialVersionUID = 1L;


}
