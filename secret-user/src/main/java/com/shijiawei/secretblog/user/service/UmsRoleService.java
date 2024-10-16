package com.shijiawei.secretblog.user.service;

import java.util.List;
import com.shijiawei.secretblog.user.entity.UmsRole;
import com.baomidou.mybatisplus.extension.service.IService;
    /**
* ClassName: UmsRoleService
* Description:
* @Create 2024/9/13 上午4:57
*/
public interface UmsRoleService extends IService<UmsRole>{


    int updateBatch(List<UmsRole> list);

    int updateBatchSelective(List<UmsRole> list);

    int batchInsert(List<UmsRole> list);

    int batchInsertSelectiveUseDefaultForNull(List<UmsRole> list);

    int deleteByPrimaryKeyIn(List<Long> list);

//    int insertOrUpdate(UmsRole record);

    int insertOrUpdateSelective(UmsRole record);

        UmsRole selectByPrimaryKey(Integer id);
    }
