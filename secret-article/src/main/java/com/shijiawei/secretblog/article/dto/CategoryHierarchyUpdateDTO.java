package com.shijiawei.secretblog.article.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * ClassName: CategoryHierarchyUpdateDTO
 * Description:
 *
 * @Create 2026/3/13 下午18:06
 */
@Data
public class CategoryHierarchyUpdateDTO {

    @NotNull(message = "目標父節點 ID 不能為空")
    private Long afterParentId;

    @NotNull(message = "目標層級不能為空")
    private Integer afterLevel;
}
