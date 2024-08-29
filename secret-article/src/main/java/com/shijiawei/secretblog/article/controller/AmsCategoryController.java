package com.shijiawei.secretblog.article.controller;

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
    @GetMapping("/list")
    public R<List<AmsCategoryTreeVo>> getCategoryTreeVos() {
        List<AmsCategoryTreeVo> amsCategoryTreeVos = amsCategoryService.getCategoryTreeVo();
//        log.info("amsCategoriesList = " + amsCategoryList);
        return R.ok(amsCategoryTreeVos);
    }

    @PostMapping("/save")
    public R saveCategory(@RequestBody AmsCategory amsCategory) {
//        log.info("amsCategory: {}", amsCategory);
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
}
