package com.shijiawei.secretblog.article.dto;

import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ClassName: AmsTagsCreateArtDTO
 * Description:
 *
 * @Create 2025/7/31 上午2:10
 */
@Data
public class AmsTagsCreateArtDTO {

    @NotBlank(message = "新增文章標籤時名稱不可為空",groups = ValidationGroups.Insert.class)
    public String name;

}
