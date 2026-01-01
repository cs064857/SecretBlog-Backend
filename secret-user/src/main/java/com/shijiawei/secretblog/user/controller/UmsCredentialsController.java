package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * (ums_credentials)表 控制層
 */
@RestController
@Tag(name = "憑證管理", description = "憑證狀態相關的 CRUD 操作")
@RequestMapping("/ums/credentials")
public class UmsCredentialsController {

    @Autowired
    private UmsCredentialsService umsCredentialsService;

    /**
     * 新增或更新
     */
    @PostMapping("/save")
    public R save(@RequestBody UmsCredentials entity) {
        umsCredentialsService.insertOrUpdateSelective(entity);
        return R.ok();
    }

    /**
     * 依主鍵查詢
     */
    @GetMapping("/{id}")
    public R<UmsCredentials> queryById(@PathVariable("id") Long id) {
        return R.ok(umsCredentialsService.selectByPrimaryKey(id));
    }
}

