package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 文章內容
 * @TableName ams_article
 */
@TableName(value ="ams_article")
@Data
public class AmsArticle implements Serializable {
    /**
     * 主鍵(雪花算法,不可為空)
     */
    @TableId
    @TableField(value = "id")
    private Long id;

    /**
     * 文章標題(不可為空,最多64字符)
     */
    @TableField(value = "title")
    private String title;

    /**
     * 文章內容(不可為空)
     */
    @TableField(value = "content")
    private String content;

    /**
     * 作者id(雪花算法,不可為空)
     */
    @TableField(value = "user_id")
    private Long user_id;

    /**
     * 文章分類id
     */
    @TableField(value = "category_id")
    private Long category_id;

    /**
     * 文章標籤id
     */
    @TableField(value = "tag_id")
    private Long tag_id;

    /**
     * 文章是否可顯示(0不顯示,1顯示)
     */
    @TableField(value = "is_show")
    private Integer is_show;

    /**
     * 創建時間
     */
    @TableField(value = "create_time")
    private LocalDateTime create_time;

    /**
     * 更新時間
     */
    @TableField(value = "update_time")
    private LocalDateTime update_time;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        AmsArticle other = (AmsArticle) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getTitle() == null ? other.getTitle() == null : this.getTitle().equals(other.getTitle()))
            && (this.getContent() == null ? other.getContent() == null : this.getContent().equals(other.getContent()))
            && (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getCategory_id() == null ? other.getCategory_id() == null : this.getCategory_id().equals(other.getCategory_id()))
            && (this.getTag_id() == null ? other.getTag_id() == null : this.getTag_id().equals(other.getTag_id()))
            && (this.getIs_show() == null ? other.getIs_show() == null : this.getIs_show().equals(other.getIs_show()))
            && (this.getCreate_time() == null ? other.getCreate_time() == null : this.getCreate_time().equals(other.getCreate_time()))
            && (this.getUpdate_time() == null ? other.getUpdate_time() == null : this.getUpdate_time().equals(other.getUpdate_time()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getTitle() == null) ? 0 : getTitle().hashCode());
        result = prime * result + ((getContent() == null) ? 0 : getContent().hashCode());
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getCategory_id() == null) ? 0 : getCategory_id().hashCode());
        result = prime * result + ((getTag_id() == null) ? 0 : getTag_id().hashCode());
        result = prime * result + ((getIs_show() == null) ? 0 : getIs_show().hashCode());
        result = prime * result + ((getCreate_time() == null) ? 0 : getCreate_time().hashCode());
        result = prime * result + ((getUpdate_time() == null) ? 0 : getUpdate_time().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", title=").append(title);
        sb.append(", content=").append(content);
        sb.append(", user_id=").append(user_id);
        sb.append(", category_id=").append(category_id);
        sb.append(", tag_id=").append(tag_id);
        sb.append(", is_show=").append(is_show);
        sb.append(", create_time=").append(create_time);
        sb.append(", update_time=").append(update_time);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}