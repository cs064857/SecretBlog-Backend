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
        ///TODO 密碼安全性判斷
        String encode = (String) this.baseMapper.selectObjs(
                new LambdaQueryWrapper<UmsUserInfo>()
                        .select(UmsUserInfo::getPassword)
                        .eq(UmsUserInfo::getId, userInfoId)
        ).stream().findFirst().orElse(null);

        if(!ObjectUtils.isEmpty(encode)){
            log.info("encode:{}", encode);
             BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
            boolean matches = bCryptPasswordEncoder.matches(password, encode.toString());//true
            log.info("matches:{}",matches);
            if(matches){//若密碼符合

            }
        }

    }
}
