package com.shijiawei.secretblog.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.mapper.UmsCredentialsMapper;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ClassName: UmsCredentialsServiceImpl
 * Description: ums_credentials 服務實作
 */
@Service
@Slf4j
public class UmsCredentialsServiceImpl extends ServiceImpl<UmsCredentialsMapper, UmsCredentials> implements UmsCredentialsService {

    @Override
    public int updateBatch(List<UmsCredentials> list) { return baseMapper.updateBatch(list); }

    @Override
    public int updateBatchSelective(List<UmsCredentials> list) { return baseMapper.updateBatchSelective(list); }

    @Override
    public int batchInsert(List<UmsCredentials> list) { return baseMapper.batchInsert(list); }

    @Override
    public int batchInsertSelectiveUseDefaultForNull(List<UmsCredentials> list) { return baseMapper.batchInsertSelectiveUseDefaultForNull(list); }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) { return baseMapper.deleteByPrimaryKeyIn(list); }

    @Override
    public int insertOrUpdateSelective(UmsCredentials record) { return baseMapper.insertOrUpdateSelective(record); }

    @Override
    public UmsCredentials selectByPrimaryKey(Long id) { return this.baseMapper.selectById(id); }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUmsCredentials(UmsCredentials entity) { this.baseMapper.insert(entity); }
}

