package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.Insert;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 
 * @TableName ams_artInfo
 */
@TableName(value ="ams_artInfo")
@Data
public class AmsArtinfo implements Serializable {
    /**
     * id(雪花算法,不可為空)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableId(value = "id")
    private Long id;

    @TableField(value = "user_name")
    private String userName;
    
    /**
     * 文章id(雪花算法,不可為空)
     */
    @TableField(value = "article_id")
    private Long articleId;

    /**
     * 用戶id(雪花算法,不可為空)
     */
    @TableField(value = "user_id")
    private Long userId;

//    /**
//     * 評論內容
//     */
//    @TableField(value = "comment")
//    private String comment;

    /**
     * 創建時間
     */
    @TableField(value = "create_time",fill= FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新時間
     */
    @TableField(value = "update_time",fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 文章是否可顯示(0不顯示,1顯示)
     */
    @TableField(value = "deleted")
    @TableLogic
    private Integer deleted;

    /**
     * 文章分類id
     */
    @NotNull(message = "文章分類ID不可為空",groups = {Update.class, Insert.class})
    @TableField(value = "category_id")
    private Long categoryId;

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
//        AmsArtinfo other = (AmsArtinfo) that;
//        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
//            && (this.getArticleId() == null ? other.getArticleId() == null : this.getArticleId().equals(other.getArticleId()))
//            && (this.getUserId() == null ? other.getUserId() == null : this.getUserId().equals(other.getUserId()))
//            && (this.getComment() == null ? other.getComment() == null : this.getComment().equals(other.getComment()))
//            && (this.getCreateTime() == null ? other.getCreateTime() == null : this.getCreateTime().equals(other.getCreateTime()))
//            && (this.getUpdateTime() == null ? other.getUpdateTime() == null : this.getUpdateTime().equals(other.getUpdateTime()))
//            && (this.getDeleted() == null ? other.getDeleted() == null : this.getDeleted().equals(other.getDeleted()));
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
//        result = prime * result + ((getArticleId() == null) ? 0 : getArticleId().hashCode());
//        result = prime * result + ((getUserId() == null) ? 0 : getUserId().hashCode());
//        result = prime * result + ((getComment() == null) ? 0 : getComment().hashCode());
//        result = prime * result + ((getCreateTime() == null) ? 0 : getCreateTime().hashCode());
//        result = prime * result + ((getUpdateTime() == null) ? 0 : getUpdateTime().hashCode());
//        result = prime * result + ((getDeleted() == null) ? 0 : getDeleted().hashCode());
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
//        sb.append(", articleId=").append(articleId);
//        sb.append(", userId=").append(userId);
//        sb.append(", comment=").append(comment);
//        sb.append(", createTime=").append(createTime);
//        sb.append(", updateTime=").append(updateTime);
//        sb.append(", deleted=").append(deleted);
//        sb.append(", serialVersionUID=").append(serialVersionUID);
//        sb.append("]");
//        return sb.toString();
//    }
}