package com.shijiawei.secretblog.article.controller;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.shijiawei.secretblog.article.dto.CategoryHierarchyUpdateDTO;
import com.shijiawei.secretblog.article.entity.AmsCategory;
import com.shijiawei.secretblog.article.service.AmsCategoryService;
import com.shijiawei.secretblog.article.vo.AmsCategoryTreeVo;
import com.shijiawei.secretblog.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsCategoryController
 * Description:
 *
 * @Create 2024/8/28 下午3:26
 */
@Slf4j
@RestController
@Tag(name = "文章分類管理", description = "文章分類相關的 CRUD 操作(分類樹相關)")
@RequestMapping("/ams")
public class AmsCategoryController {
    @Autowired
    AmsCategoryService amsCategoryService;

    @Operation(summary = "分類樹相關-樹形組件數據請求")
    @GetMapping("/categories/tree")
    public R<List<AmsCategoryTreeVo>> getTreeCategoryVos() {
        List<AmsCategoryTreeVo> amsCategoryTreeVos = amsCategoryService.getTreeCategoryVo();
        return R.ok(amsCategoryTreeVos);
    }

    @Operation(summary = "分類樹相關-新增分類")
    @PostMapping("/categories")
    public R saveCategory(@RequestBody AmsCategory amsCategory) {
        log.info("amsCategory: {}", amsCategory);
        amsCategoryService.saveCategory(amsCategory);
        return R.ok();
    }

    @Operation(summary = "分類樹相關-邏輯刪除分類")
    @DeleteMapping("/categories/{id}")
    public R deleteCategory(@PathVariable Long id) {
        amsCategoryService.deleteById(id);
        return R.ok();
    }

    //調整分類層級與父節點，    //before(原數據),afterParentId(原數據應變更ParentId),afterLevel(原數據應變更Level)
    @Operation(summary = "分類樹相關-調整分類層級與父節點")
    @PutMapping("/categories/{id}/hierarchy")
    public R updateCategory(@PathVariable Long id, @Validated @RequestBody CategoryHierarchyUpdateDTO dto) {
        amsCategoryService.updateCategory(id, dto.getAfterParentId(), dto.getAfterLevel());
        return R.ok();
    }

    /**
     * 分類樹相關-修改樹形分類名稱
     * @param id
     * @param categoryName
     * @return
     */

    @PutMapping("/categories/{id}")
    public R updateTreeCategory(@PathVariable Long id,@RequestBody String categoryName) {
        log.info("id:{}", id);
        log.info("categoryName {}", categoryName);
        JsonNode jsonNode = JacksonUtils.toObj(categoryName);
        String str = jsonNode.get("categoryName").asText();
        log.info("str: {}", str);

        amsCategoryService.updateTreeCategoryName(id, str);
        return R.ok();
    }

}
