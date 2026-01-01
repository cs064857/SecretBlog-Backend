package com.shijiawei.secretblog.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.user.entity.UmsStatus;
import com.shijiawei.secretblog.user.mapper.UmsStatusMapper;
import com.shijiawei.secretblog.user.service.UmsStatusService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ClassName: UmsStatusServiceImpl
 * Description: ums_status 服務實作
 */
@Service
@Slf4j
public class UmsStatusServiceImpl extends ServiceImpl<UmsStatusMapper, UmsStatus> implements UmsStatusService {

    @Override
    public int updateBatch(List<UmsStatus> list) { return baseMapper.updateBatch(list); }

    @Override
    public int updateBatchSelective(List<UmsStatus> list) { return baseMapper.updateBatchSelective(list); }

    @Override
    public int batchInsert(List<UmsStatus> list) { return baseMapper.batchInsert(list); }

    @Override
    public int batchInsertSelectiveUseDefaultForNull(List<UmsStatus> list) { return baseMapper.batchInsertSelectiveUseDefaultForNull(list); }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) { return baseMapper.deleteByPrimaryKeyIn(list); }

    @Override
    public int insertOrUpdateSelective(UmsStatus record) { return baseMapper.insertOrUpdateSelective(record); }

    @Override
    public UmsStatus selectByPrimaryKey(Long id) { return this.baseMapper.selectById(id); }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUmsStatus(UmsStatus entity) { this.baseMapper.insert(entity); }
}

