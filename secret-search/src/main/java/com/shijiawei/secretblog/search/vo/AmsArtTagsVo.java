package com.shijiawei.secretblog.search.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * ClassName: AmsArtTagsVo
 * Description:
 *
 * @Create 2025/12/9 下午9:30
 */
@Data
public class AmsArtTagsVo {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    private String name;

}
