package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * ClassName: AmsSaveArtInfoVo
 * Description:
 *
 * @Create 2024/9/12 下午3:48
 */
@Data
public class AmsSaveArtInfoVo {
    /**
     * 文章id(雪花算法,不可為空)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long articleId;

    /**
     * 用戶id(雪花算法,不可為空)
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;

    /**
     * 留言內容
     */
    private String comment;
}
