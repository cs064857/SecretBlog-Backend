package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* ClassName: UmsUserMapper
* Description:
* @Create 2024/9/14 上午3:57
*/
@Mapper
public interface UmsUserMapper extends BaseMapper<UmsUser> {

    List<UserBasicDTO> selectUserBasicInfoByIds(@Param("ids") List<Long> ids);

//    int updateBatch(List<UmsUser> list);
//
//    int updateBatchSelective(List<UmsUser> list);
//
//    int batchInsert(@Param("list") List<UmsUser> list);
//
//    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<UmsUser> list);
//
//    int deleteByPrimaryKeyIn(List<Long> list);
//
////    int insertOrUpdate(UmsUser record);
//
//    int insertOrUpdateSelective(UmsUser record);
//
////    @Select("SELECT * FROM ums_user")
////    List<UmsUser> selectDeletedUsers();
}