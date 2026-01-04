package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * ClassName: UmsCredentialsMapper
 * Description: ums_credentials 資料訪問層
 */
@Mapper
public interface UmsCredentialsMapper extends BaseMapper<UmsCredentials> {
    int updateBatch(List<UmsCredentials> list);
    int updateBatchSelective(List<UmsCredentials> list);
    int batchInsert(@Param("list") List<UmsCredentials> list);
    int batchInsertSelectiveUseDefaultForNull(@Param("list") List<UmsCredentials> list);
    int deleteByPrimaryKeyIn(List<Long> list);
    int insertOrUpdateSelective(UmsCredentials record);
}

