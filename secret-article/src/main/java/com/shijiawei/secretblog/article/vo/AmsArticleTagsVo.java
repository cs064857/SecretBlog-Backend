package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: AmsArticleTagsVo
 * Description:
 *
 * @Create 2025/9/23 上午2:29
 */
@Data
public class AmsArticleTagsVo {



    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;



}
