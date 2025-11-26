package com.shijiawei.secretblog.article.vo;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ClassName: AmsArtAction
 * Description:
 *
 * @Create 2025/11/25 下午10:00
 */

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class AmsArtActionVo{

    /**
     * 是否點讚 (0:未點讚, 1:已點讚)
     */
    private Byte isLiked;


    private Byte isBookmarked;


}
