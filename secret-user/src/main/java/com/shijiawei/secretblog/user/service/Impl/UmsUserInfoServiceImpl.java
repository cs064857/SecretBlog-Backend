package com.shijiawei.secretblog.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.mapper.UmsUserInfoMapper;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * ClassName: UmsUserInfoServiceImpl
 * Description:
 *
 * @Create 2024/9/14 上午4:09
 */
@Service
@Slf4j
public class UmsUserInfoServiceImpl extends ServiceImpl<UmsUserInfoMapper, UmsUserInfo> implements UmsUserInfoService {

    @Override
    public int updateBatch(List<UmsUserInfo> list) {
        return baseMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<UmsUserInfo> list) {
        return baseMapper.updateBatchSelective(list);
    }

    @Override
    public int batchInsert(List<UmsUserInfo> list) {
        return baseMapper.batchInsert(list);
    }

    @Override
    public int batchInsertSelectiveUseDefaultForNull(List<UmsUserInfo> list) {
        return baseMapper.batchInsertSelectiveUseDefaultForNull(list);
    }

    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) {
        return baseMapper.deleteByPrimaryKeyIn(list);
    }

//    @Override
//    public int insertOrUpdate(UmsUserInfo record) {
//        return baseMapper.insertOrUpdate(record);
//    }

    @Override
    public int insertOrUpdateSelective(UmsUserInfo record) {
        return baseMapper.insertOrUpdateSelective(record);
    }

    @Override
    public UmsUserInfo selectByPrimaryKey(Integer id) {
        return this.baseMapper.selectById(id);
    }

    @Override
    public void updateUserInfo(UmsUserInfo umsUserInfo) {
        log.info("umsUserInfo:{}", umsUserInfo);
        this.baseMapper.updateById(umsUserInfo);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUmsUserInfo(UmsUserInfo userInfo) {
        this.baseMapper.insert(userInfo);
    }

    @Override
    public List<UmsUserInfo> listUmsUserInfo() {
        return this.baseMapper.selectList(new LambdaQueryWrapper<UmsUserInfo>());
    }
    /**
     * 判斷密碼是否符合
     */
    @Override
    public void passwordMatchesDatabase(Long userInfoId, String password){
        ///TODO DDL變更: UmsUserInfo 已無 password 欄位，原密碼校驗流程需重新設計（例如移至其它表或改用認證服務）
        // 暫時不進行任何校驗，直接返回
    }
}
