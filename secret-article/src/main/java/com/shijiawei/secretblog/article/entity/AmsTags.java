package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * ClassName: AmsTags
 * Description:
 *
 * @Create 2025/7/28 下午10:25
 */
@Data
@TableName(value= "ams_tags")
public class AmsTags {

    @NotNull(message = "主鍵ID不可為空",groups = {ValidationGroups.Delete.class,ValidationGroups.Update.class})
    @TableId
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @TableField(value = "id")
    private Long id;

    @NotNull(message = "主鍵ID不可為空",groups = {ValidationGroups.Insert.class})
    @TableField(value = "name")
    private String name;
    @TableField(value = "create_at",fill = FieldFill.INSERT)
    private LocalDateTime createAt;
    @TableField(value = "update_at",fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateAt;

}
