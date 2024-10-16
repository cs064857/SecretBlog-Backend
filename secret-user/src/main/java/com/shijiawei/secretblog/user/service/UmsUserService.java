package com.shijiawei.secretblog.user.service;

import java.util.List;

import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;

/**
* ClassName: UmsUserService
* Description:
* @Create 2024/9/14 上午3:57
*/
public interface UmsUserService extends IService<UmsUser>{


    int updateBatch(List<UmsUser> list);

    int updateBatchSelective(List<UmsUser> list);

    int batchInsert(List<UmsUser> list);

    int batchInsertSelectiveUseDefaultForNull(List<UmsUser> list);

    int deleteByPrimaryKeyIn(List<Long> list);

//    int insertOrUpdate(UmsUser record);

    int insertOrUpdateSelective(UmsUser record);

        UmsUser selectByPrimaryKey(Integer id);

        void saveUmsUser(UmsSaveUserVo umsUser);

    List<UmsUserDetailsDTO> listUmsUserDetails();

    List<UmsUser> listUmsUser();

    void deleteUmsUserDetails(List<Long> userIdList);

    void updateUmsUserDetails(UmsUpdateUserDetailsVO updateUserDetailsVO, Long userId, Long userInfoId);
}
