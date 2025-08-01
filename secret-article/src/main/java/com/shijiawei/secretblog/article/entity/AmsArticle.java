package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.esotericsoftware.kryo.util.IntArray;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 文章內容
 * @TableName ams_article
 */
@TableName(value ="ams_article")
@Data
public class AmsArticle implements Serializable {
    /**
     * 主鍵(雪花算法)
     */
    @NotNull(message = "修改資料主鍵不可為空",groups = {Update.class})
    @TableId
    @JsonFormat(shape= JsonFormat.Shape.STRING)
    @TableField(value = "id")
    private Long id;

    /**
     * 文章標題(不可為空,最多64字符)
     */
    @NotBlank(message = "新增時文章標題不可為空",groups = {Insert.class})
    @TableField(value = "title")
    private String title;

    /**
     * 文章內容(不可為空)
     */
    @NotBlank(message = "新增時文章內容不可為空",groups = {Insert.class})
    @TableField(value = "content")
    private String content;


/**
 * 20250728移除
 */


    /**
     * 作者id(雪花算法,不可為空)
     */
//    @NotNull(message = "作者ID不可為空",groups = {Insert.class})

//    @TableField(value = "user_Id")
//    private Long userId;

    /**
     * 文章分類id
     */
//    @NotNull(message = "文章分類ID不可為空",groups = {Update.class,Insert.class})
//    @TableField(value = "category_id")
//    private Long categoryId;

    /**
     * 文章標籤id
     */
//    @NotNull(message = "文章標籤不可為空",groups = {Insert.class})
    /**
     * 20250728移除
     */
//    @TableField(value = "tag_id")
//    private Long tagId;

    /**
     * 文章是否可顯示(0不顯示,1顯示)
     */
    /**
     * 20250728移除
     */
//    @TableLogic
//    @TableField(value = "deleted")
//    private Integer deleted;

    /**
     * 創建時間
     */

    //    @TableField(value = "create_time",fill= FieldFill.INSERT)
//    private LocalDateTime createTime;

    /**
     * 更新時間
     */

    //    @TableField(value = "update_time",fill= FieldFill.INSERT_UPDATE)
//    private LocalDateTime updateTime;



//    @TableField(exist = false)
//    private static final long serialVersionUID = 1L;
//
//    @Override
//    public boolean equals(Object that) {
//        if (this == that) {
//            return true;
//        }
//        if (that == null) {
//            return false;
//        }
//        if (getClass() != that.getClass()) {
//            return false;
//        }
//        AmsArticle other = (AmsArticle) that;
//        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
//            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
//            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
//            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
//            && (this.getCategoryId() == null ? other.getCategoryId() == null : this.getCategoryId().equals(other.getCategoryId()))
//            && (this.getTagId() == null ? other.getTagId() == null : this.getTagId().equals(other.getTagId()))
//            && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()))
//            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
//            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()));
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
//        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
//        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
//        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
//        result = prime * result + ((getCategoryId() == null) ? 0 : getCategoryId().hashCode());
//        result = prime * result + ((getTagId() == null) ? 0 : getTagId().hashCode());
//        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
//        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
//        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(getClass().getSimpleName());
//        sb.append(" [");
//        sb.append("Hash = ").append(hashCode());
//        sb.append(", id=").append(id);
//        sb.append(", title=").append(title);
//        sb.append(", content=").append(content);
//        sb.append(", userId=").append(userId);
//        sb.append(", categoryId=").append(categoryId);
//        sb.append(", tagId=").append(tagId);
//        sb.append(", deleted=").append(deleted);
//        sb.append(", createTime=").append(createTime);
//        sb.append(", updateTime=").append(updateTime);
//        sb.append(", serialVersionUID=").append(serialVersionUID);
//        sb.append("]");
//        return sb.toString();
//    }
}