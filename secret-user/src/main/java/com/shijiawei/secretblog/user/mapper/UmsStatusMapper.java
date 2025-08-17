package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsStatus;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ClassName: UmsStatusMapper
 * Description: ums_status 資料訪問層
 * 註解均採用繁體中文
 */
@Mapper
public interface UmsStatusMapper extends BaseMapper<UmsStatus> {
    int updateBatch(List<UmsStatus> list);
    int updateBatchSelective(List<UmsStatus> list);
    int batchInsert(@Param("list") List<UmsStatus> list);
    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<UmsStatus> list);
    int deleteByPrimaryKeyIn(List<Long> list);
    int insertOrUpdateSelective(UmsStatus record);
}

