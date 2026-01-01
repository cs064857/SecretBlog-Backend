package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.service.Impl.UmsUserInfoServiceImpl;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

/**
 * (ums_userInfo)表控制層
 */
@RestController
@Tag(name = "使用者資訊管理", description = "使用者資訊相關的 CRUD 操作")
@RequestMapping("/ums/userInfo")
public class UmsUserInfoController {
    /**
     * 服務對象
     */
    @Autowired
    private UmsUserInfoService umsUserInfoService;

    /**
     * 通過主鍵查詢單條數據
     *
     * @param id 主鍵
     * @return 單條數據
     */
    @GetMapping("{id}")
    public R<UmsUserInfo> queryById(@PathVariable("id") Integer id) {
        UmsUserInfo umsUserInfo= this.umsUserInfoService.selectByPrimaryKey(id);
        return R.ok(umsUserInfo);
    }

    /**
     * 新增數據
     *
     * @param umsUserInfo 實體
     * @return 新增結果
     */
    @PostMapping(value = "/add")
    public R add(UmsUserInfo umsUserInfo) {
        this.umsUserInfoService.insertOrUpdateSelective(umsUserInfo);
        return R.ok();
    }

    /**
     * 編輯數據
     *
     * @param umsUserInfo 實體
     * @return 編輯結果
     */
    @PutMapping(value = "/update")
    public R edit(UmsUserInfo umsUserInfo) {
        this.umsUserInfoService.updateUserInfo(umsUserInfo);
//        this.umsUserInfoService.updateByPrimaryKeySelective(umsUserInfo);
        return R.ok();
    }

    /**
     * 刪除數據
     *
     * @param id 主鍵
     * @return 刪除是否成功
     */
    @PostMapping(value = "/delete/{id}")
    public R deleteById(@PathVariable("id") Long id) {
        this.umsUserInfoService.deleteByPrimaryKeyIn(Collections.singletonList(id));
        return R.ok();
    }

    /**
     * 獲取所有使用者資訊
     * @return
     */
    @GetMapping
    public R<List<UmsUserInfo>> listUmsUserInfo() {
        List<UmsUserInfo> umsUserInfoList= umsUserInfoService.listUmsUserInfo();
        return R.ok(umsUserInfoList);
    }
}
