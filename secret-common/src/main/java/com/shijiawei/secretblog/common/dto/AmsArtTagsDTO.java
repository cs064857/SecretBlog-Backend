package com.shijiawei.secretblog.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 文章標籤共享 DTO
 * 用於微服務間數據傳輸，統一 secret-article 與 secret-search 的標籤資料結構
 */
@Data
public class AmsArtTagsDTO {

    /**
     * 標籤ID
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long id;

    /**
     * 標籤名稱
     */
    private String name;
}
