package com.shijiawei.secretblog.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.user.entity.UmsAuths;
import java.util.List;

/**
 * ClassName: UmsAuthsService
 * Description: ums_auths 服務介面
 */
public interface UmsAuthsService extends IService<UmsAuths> {

    int updateBatch(List<UmsAuths> list);
    int updateBatchSelective(List<UmsAuths> list);
    int batchInsert(List<UmsAuths> list);
    int batchInsertSelectiveUseDefaultForNull(List<UmsAuths> list);
    int deleteByPrimaryKeyIn(List<Long> list);
    int insertOrUpdateSelective(UmsAuths record);

    UmsAuths selectByPrimaryKey(Long id);

    void saveUmsAuths(UmsAuths entity);
}

