package com.shijiawei.secretblog.user.service.Impl;

import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shijiawei.secretblog.user.mapper.UmsRoleMapper;
import com.shijiawei.secretblog.user.entity.UmsRole;
import com.shijiawei.secretblog.user.service.UmsRoleService;

/**
 * ClassName: UmsRoleServiceImpl
 * Description:
 *
 * @Create 2024/9/13 上午4:57
 */
@Service
public class UmsRoleServiceImpl extends ServiceImpl<UmsRoleMapper, UmsRole> implements UmsRoleService {

    @Override
    public int updateBatch(List<UmsRole> list) {
        return baseMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<UmsRole> list) {
        return baseMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<UmsRole> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int batchInsertSelectiveUseDefaultForNull(List<UmsRole> list) {
        return baseMapper.batchInsertSelectiveUseDefaultForNull(list);
    }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) {
        return baseMapper.deleteByPrimaryKeyIn(list);
    }

//    @Override
//    public int insertOrUpdate(UmsRole record) {
//
//        return baseMapper.insertOrUpdate(record);
//    }


    @Override
    public int insertOrUpdateSelective(UmsRole record) {
        return baseMapper.insertOrUpdateSelective(record);
    }

    @Override
    public UmsRole selectByPrimaryKey(Integer id) {
        return this.baseMapper.selectById(id);
    }
}
