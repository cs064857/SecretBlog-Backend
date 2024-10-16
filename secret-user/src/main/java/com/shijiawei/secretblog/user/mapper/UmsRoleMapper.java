package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsRole;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* ClassName: UmsRoleMapper
* Description:
* @Create 2024/9/13 上午4:57
*/
@Mapper
public interface UmsRoleMapper extends BaseMapper<UmsRole> {
    int updateBatch(List<UmsRole> list);

    int updateBatchSelective(List<UmsRole> list);

    int batchInsert(@Param("list") List<UmsRole> list);

    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<UmsRole> list);

    int deleteByPrimaryKeyIn(List<Long> list);

//    int insertOrUpdate(UmsRole record);

    int insertOrUpdateSelective(UmsRole record);
}