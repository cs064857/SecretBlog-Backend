package com.shijiawei.secretblog.user.service;

import java.util.List;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
    /**
* ClassName: UmsUserInfoService
* Description:
* @Create 2024/9/14 上午4:09
*/
public interface UmsUserInfoService extends IService<UmsUserInfo>{


    int updateBatch(List<UmsUserInfo> list);

    int updateBatchSelective(List<UmsUserInfo> list);

    int batchInsert(List<UmsUserInfo> list);

    int batchInsertSelectiveUseDefaultForNull(List<UmsUserInfo> list);

    int deleteByPrimaryKeyIn(List<Long> list);

//    int insertOrUpdate(UmsUserInfo record);

    int insertOrUpdateSelective(UmsUserInfo record);

        UmsUserInfo selectByPrimaryKey(Integer id);

        void updateUserInfo(UmsUserInfo umsUserInfo);

        void saveUmsUserInfo(UmsUserInfo userInfo);

        List<UmsUserInfo> listUmsUserInfo();

        void passwordMatchesDatabase(Long userInfoId, String password);
    }
