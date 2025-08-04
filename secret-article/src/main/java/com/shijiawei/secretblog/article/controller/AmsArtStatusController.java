package com.shijiawei.secretblog.article.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.shijiawei.secretblog.article.entity.AmsArtStatus;
import com.shijiawei.secretblog.article.service.AmsArtStatusService;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsArtStatusController
 * Description: 文章狀態管理控制器 - 提供完整的CRUD操作
 *
 * @Create 2025/8/3 上午2:37
 */
@Slf4j
@RequestMapping("/article/artstatus")
@RestController
@RequiredArgsConstructor
public class AmsArtStatusController {

    private final AmsArtStatusService amsArtStatusService;

    /**
     * 單筆新增 - POST /article/artstatus
     */
    @PostMapping
    public R<AmsArtStatus> save(@RequestBody @Validated AmsArtStatus amsArtStatus) {
        log.info("新增文章狀態: {}", amsArtStatus);
        boolean saved = amsArtStatusService.save(amsArtStatus);
        if (saved && amsArtStatus.getId() != null) {
            return R.ok("新增成功", amsArtStatus);
        }
        return R.error("新增失敗");
    }

    /**
     * 單筆查詢 - GET /article/artstatus/{id}
     */
    @GetMapping("/{id}")
    public R<AmsArtStatus> getById(@PathVariable Long id) {
        log.info("查詢文章狀態: ID={}", id);
        AmsArtStatus artStatus = amsArtStatusService.getById(id);
        return artStatus != null ? R.ok("查詢成功", artStatus) : R.error("數據不存在");
    }

    /**
     * 批量查詢 - GET /article/artstatus/batch
     * 通過ID列表查詢 multiple records
     */
    @GetMapping("/batch")
    public R<List<AmsArtStatus>> getByIds(@RequestParam List<Long> ids) {
        log.info("批量查詢文章狀態: IDs={}", ids);
        List<AmsArtStatus> artStatusList = amsArtStatusService.listByIds(ids);
        return R.ok("查詢成功", artStatusList);
    }

    /**
     * 條件查詢 - GET /article/artstatus/list
     * 根據文章ID查詢
     */
    @GetMapping("/list")
    public R<List<AmsArtStatus>> listByArticleId() {

        List<AmsArtStatus> list = amsArtStatusService.list();
        return R.ok("查詢成功", list);
    }


    /**
     * 條件查詢 - GET /article/artstatus/list
     * 根據文章ID查詢
     */
    @GetMapping("/getByIds")
    public R<List<AmsArtStatus>> getByArticleIds(@RequestParam(required = true) List<Long> articleIds) {
        log.info("條件查詢文章狀態: articleIds={}", articleIds);

        LambdaQueryWrapper<AmsArtStatus> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.in(articleIds != null, AmsArtStatus::getArticleId, articleIds)
                .orderByDesc(AmsArtStatus::getId);

        List<AmsArtStatus> list = amsArtStatusService.list(queryWrapper);
        return R.ok("查詢成功", list);
    }

    /**
     * 條件查詢 - GET /article/artstatus/list
     * 根據文章ID查詢
     */
    @GetMapping("/getById")
    public R<AmsArtStatus> getByArticleId(@RequestParam(required = true) Long articleId) {
        log.info("條件查詢文章狀態: articleId={}", articleId);

        LambdaQueryWrapper<AmsArtStatus> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(articleId != null, AmsArtStatus::getArticleId, articleId)
                .orderByDesc(AmsArtStatus::getId);

        AmsArtStatus amsArtStatus = amsArtStatusService.getOne(queryWrapper);
        return R.ok("查詢成功", amsArtStatus);
    }

    /**
     * 單筆更新 - PUT /article/artstatus/{id}
     */
    @PutMapping("/{id}")
    public R<AmsArtStatus> updateById(
            @PathVariable Long id,
            @RequestBody @Validated(value = {ValidationGroups.Update.class}) AmsArtStatus amsArtStatus) {
        log.info("更新文章狀態: ID={}, 數據={}", id, amsArtStatus);

        // 設置主鍵
        amsArtStatus.setId(id);

        // 檢查是否存在
        if (!amsArtStatusService.updateById(amsArtStatus)) {
            return R.error("更新失敗，數據可能不存在");
        }

        // 返回最新數據
        AmsArtStatus updated = amsArtStatusService.getById(id);
        return R.ok("更新成功", updated);
    }

    /**
     * 單筆刪除 - DELETE /article/artstatus/{id}
     */
    @DeleteMapping("/{id}")
    public R deleteById(@PathVariable Long id) {
        log.info("刪除文章狀態: ID={}", id);

        if (!amsArtStatusService.removeById(id)) {
            return R.error("刪除失敗，數據可能不存在");
        }
        return R.ok("刪除成功");
    }

    /**
     * 批量刪除 - DELETE /article/artstatus/batch
     * 通過ID列表刪除
     */
    @DeleteMapping("/batch")
    public R deleteBatch(@RequestBody List<Long> ids) {
        log.info("批量刪除文章狀態: IDs={}", ids);

        if (ids == null || ids.isEmpty()) {
            return R.error("刪除失敗，ID列表為空");
        }

        boolean removed = amsArtStatusService.removeByIds(ids);
        return removed ? R.ok("批量刪除成功") : R.error("批量刪除失敗");
    }
}
