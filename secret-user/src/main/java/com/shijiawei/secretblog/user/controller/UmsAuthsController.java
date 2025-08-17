package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.entity.UmsAuths;
import com.shijiawei.secretblog.user.service.UmsAuthsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * (ums_auths)表 控制層
 * 註解均採用繁體中文
 */
@RestController
@RequestMapping("/ums/auths")
public class UmsAuthsController {

    @Autowired
    private UmsAuthsService umsAuthsService;

    /**
     * 新增或更新
     */
    @PostMapping("/save")
    public R save(@RequestBody UmsAuths entity) {
        umsAuthsService.insertOrUpdateSelective(entity);
        return R.ok();
    }

    /**
     * 依主鍵查詢
     */
    @GetMapping("/{id}")
    public R<UmsAuths> queryById(@PathVariable("id") Long id) {
        return R.ok(umsAuthsService.selectByPrimaryKey(id));
    }
}

