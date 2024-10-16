package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* ClassName: UmsUserInfoMapper
* Description:
* @Create 2024/9/14 上午4:09
*/
@Mapper
public interface UmsUserInfoMapper extends BaseMapper<UmsUserInfo> {
    int updateBatch(List<UmsUserInfo> list);

    int updateBatchSelective(List<UmsUserInfo> list);

    int batchInsert(@Param("list") List<UmsUserInfo> list);

    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<UmsUserInfo> list);

    int deleteByPrimaryKeyIn(List<Long> list);

//    int insertOrUpdate(UmsUserInfo record);

    int insertOrUpdateSelective(UmsUserInfo record);
}