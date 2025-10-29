package com.shijiawei.secretblog.article.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * ClassName: AmsArtTagsVo
 * Description:
 *
 * @Create 2025/9/23 上午2:29
 */
@Data
public class AmsArtTagsVo {



    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;



}
