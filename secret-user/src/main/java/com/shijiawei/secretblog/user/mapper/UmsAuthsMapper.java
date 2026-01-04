package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsAuths;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ClassName: UmsAuthsMapper
 * Description: ums_auths 資料訪問層
 */
@Mapper
public interface UmsAuthsMapper extends BaseMapper<UmsAuths> {
    int updateBatch(List<UmsAuths> list);
    int updateBatchSelective(List<UmsAuths> list);
    int batchInsert(@Param("list") List<UmsAuths> list);
    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<UmsAuths> list);
    int deleteByPrimaryKeyIn(List<Long> list);
    int insertOrUpdateSelective(UmsAuths record);
}

