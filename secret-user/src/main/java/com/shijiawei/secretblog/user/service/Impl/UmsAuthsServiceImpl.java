package com.shijiawei.secretblog.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.user.entity.UmsAuths;
import com.shijiawei.secretblog.user.mapper.UmsAuthsMapper;
import com.shijiawei.secretblog.user.service.UmsAuthsService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ClassName: UmsAuthsServiceImpl
 * Description: ums_auths 服務實作
 */
@Service
@Slf4j
public class UmsAuthsServiceImpl extends ServiceImpl<UmsAuthsMapper, UmsAuths> implements UmsAuthsService {

    @Override
    public int updateBatch(List<UmsAuths> list) { return baseMapper.updateBatch(list); }

    @Override
    public int updateBatchSelective(List<UmsAuths> list) { return baseMapper.updateBatchSelective(list); }

    @Override
    public int batchInsert(List<UmsAuths> list) { return baseMapper.batchInsert(list); }

    @Override
    public int batchInsertSelectiveUseDefaultForNull(List<UmsAuths> list) { return baseMapper.batchInsertSelectiveUseDefaultForNull(list); }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) { return baseMapper.deleteByPrimaryKeyIn(list); }

    @Override
    public int insertOrUpdateSelective(UmsAuths record) { return baseMapper.insertOrUpdateSelective(record); }

    @Override
    public UmsAuths selectByPrimaryKey(Long id) { return this.baseMapper.selectById(id); }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUmsAuths(UmsAuths entity) { this.baseMapper.insert(entity); }
}

