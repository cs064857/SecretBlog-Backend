package com.shijiawei.secretblog.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.user.entity.UmsStatus;
import java.util.List;

/**
 * ClassName: UmsStatusService
 * Description: ums_status 服務介面
 */
public interface UmsStatusService extends IService<UmsStatus> {

    int updateBatch(List<UmsStatus> list);
    int updateBatchSelective(List<UmsStatus> list);
    int batchInsert(List<UmsStatus> list);
    int batchInsertSelectiveUseDefaultForNull(List<UmsStatus> list);
    int deleteByPrimaryKeyIn(List<Long> list);
    int insertOrUpdateSelective(UmsStatus record);

    UmsStatus selectByPrimaryKey(Long id);

    void saveUmsStatus(UmsStatus entity);
}

