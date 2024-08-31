package com.shijiawei.secretblog.article.controller;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.alibaba.nacos.shaded.com.google.protobuf.Descriptors;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.shijiawei.secretblog.article.entity.AmsCategory;
import com.shijiawei.secretblog.article.service.AmsCategoryService;
import com.shijiawei.secretblog.article.vo.AmsCategoryTreeVo;
import com.shijiawei.secretblog.common.utils.R;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/article/category")
public class AmsCategoryController {
    @Autowired
    AmsCategoryService amsCategoryService;

    @Operation(summary = "分類樹形組件數據請求")
    @GetMapping("/tree/list")
    public R<List<AmsCategoryTreeVo>> getTreeCategoryVos() {
        List<AmsCategoryTreeVo> amsCategoryTreeVos = amsCategoryService.getTreeCategoryVo();
//        log.info("amsCategoriesList = " + amsCategoryList);
        return R.ok(amsCategoryTreeVos);
    }

    @PostMapping("/save")
    public R saveCategory(@RequestBody AmsCategory amsCategory) {
        log.info("amsCategory: {}", amsCategory);
        amsCategoryService.saveCategory(amsCategory);
        return R.ok();
    }

    @PostMapping("/delete/{id}")
    public R deleteCategory(@PathVariable Long id) {
        //邏輯刪除
        amsCategoryService.deleteById(id);
        return R.ok();
    }
    //before(原數據),afterParentId(原數據應變更ParentId),afterLevel(原數據應變更Level)
    @PostMapping("/update/{beforeId}/{afterParentId}/{afterLevel}")
    public R updateCategory(@PathVariable Long beforeId,@PathVariable Long afterParentId,@PathVariable Integer afterLevel) {
//        log.info("id: {}", beforeId);
//        log.info("NewParentId: {}", afterParentId);
//        log.info("afterLevel: {}", afterLevel);
        amsCategoryService.updateCategory(beforeId,afterParentId,afterLevel);
        return R.ok();
    }

    /**
     * 修改樹形分類數據
     * @param id
     * @param categoryName
     * @return
     */

    @PutMapping("/{id}")

    public R updateTreeCategory(@PathVariable Long id,@RequestBody String categoryName) {
        log.info("id:{}",id);
        log.info("categoryName {}", categoryName);
        String str = JacksonUtils.toObj(categoryName).asText();
        log.info("str: {}", str);

        amsCategoryService.updateTreeCategoryName(id,str);
        return R.ok();
    }
}
