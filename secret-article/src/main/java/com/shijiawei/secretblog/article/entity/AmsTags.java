package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.core.conditions.update.Update;
import com.baomidou.mybatisplus.core.injector.methods.Insert;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;
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

    /**
     * 邏輯刪除(0未刪除,1被刪除)
     */
    @TableField(value = "deleted")
    @Schema(description="邏輯刪除(0未刪除,1被刪除)")
    @NotNull(message = "邏輯刪除(0未刪除,1被刪除)不能为null")
    private Byte deleted;

}
