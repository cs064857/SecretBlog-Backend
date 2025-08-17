package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.entity.UmsStatus;
import com.shijiawei.secretblog.user.service.UmsStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * (ums_status)表 控制層
 * 註解均採用繁體中文
 */
@RestController
@RequestMapping("/ums/status")
public class UmsStatusController {

    @Autowired
    private UmsStatusService umsStatusService;

    /**
     * 新增或更新
     */
    @PostMapping("/save")
    public R save(@RequestBody UmsStatus entity) {
        umsStatusService.insertOrUpdateSelective(entity);
        return R.ok();
    }

    /**
     * 依主鍵查詢
     */
    @GetMapping("/{id}")
    public R<UmsStatus> queryById(@PathVariable("id") Long id) {
        return R.ok(umsStatusService.selectByPrimaryKey(id));
    }
}

