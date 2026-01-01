package com.shijiawei.secretblog.user.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.user.entity.UmsRole;
import com.shijiawei.secretblog.user.service.Impl.UmsRoleServiceImpl;
import com.shijiawei.secretblog.user.service.UmsRoleService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * (ums_role)表控制層

 */
@RestController
@Slf4j
@Tag(name = "角色管理", description = "角色狀態相關的 CRUD 操作")
@RequestMapping("/ums/role")
public class UmsRoleController {
    /**
     * 服務對象
     */
    @Autowired
    private UmsRoleService umsRoleService;

    @PostMapping()
    public R saveRole(@RequestBody UmsRole umsRole) {
        umsRole.setRoleName("普通使用者");
        umsRoleService.save(umsRole);
        return R.ok();
    }

    /**
     * 通過主鍵查詢單條數據
     *
     * @param id 主鍵
     * @return 單條數據
     */
    @GetMapping("selectOne")
    public R<UmsRole> selectOne(Integer id) {
        UmsRole umsRole= umsRoleService.selectByPrimaryKey(id);
        return R.ok(umsRole);
    }

    @GetMapping
    public R<List<UmsRole>> listRole() {
        List<UmsRole> umsRoleList = umsRoleService.list(new LambdaQueryWrapper<UmsRole>().eq(UmsRole::getDeleted,0));
        return R.ok(umsRoleList);
    }

}
