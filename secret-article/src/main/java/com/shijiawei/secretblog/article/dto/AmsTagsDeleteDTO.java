package com.shijiawei.secretblog.article.dto;

import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * ClassName: AmsTagDeleteDTO
 * Description:
 *
 * @Create 2025/7/28 下午11:23
 */
@Data
public class AmsTagsDeleteDTO {


    @NotNull(message = "主鍵IDS不可為空",groups = {ValidationGroups.Delete.class})
    private List<Long> ids;

}
