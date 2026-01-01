package com.shijiawei.secretblog.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import java.util.List;

/**
 * ClassName: UmsCredentialsService
 * Description: ums_credentials 服務介面
 */
public interface UmsCredentialsService extends IService<UmsCredentials> {

    int updateBatch(List<UmsCredentials> list);
    int updateBatchSelective(List<UmsCredentials> list);
    int batchInsert(List<UmsCredentials> list);
    int batchInsertSelectiveUseDefaultForNull(List<UmsCredentials> list);
    int deleteByPrimaryKeyIn(List<Long> list);
    int insertOrUpdateSelective(UmsCredentials record);

    UmsCredentials selectByPrimaryKey(Long id);

    void saveUmsCredentials(UmsCredentials entity);
}

