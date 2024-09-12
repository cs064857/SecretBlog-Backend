package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.TableField;
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
    private Long articleId;

    /**
     * 用戶id(雪花算法,不可為空)
     */
    private Long userId;

    /**
     * 評論內容
     */
    private String comment;
}
