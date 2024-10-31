package com.shijiawei.secretblog.user.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.entity.UmsRole;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.enumValue.Gender;
import com.shijiawei.secretblog.user.enumValue.Role;
import com.shijiawei.secretblog.user.service.UmsRoleService;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
import com.shijiawei.secretblog.user.service.UmsUserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
* ClassName: UmsUserServiceImpl
* Description:
* @Create 2024/9/14 上午3:57
*/
@Service
@Slf4j
public class UmsUserServiceImpl extends ServiceImpl<UmsUserMapper, UmsUser> implements UmsUserService{

    @Autowired
    private UmsUserInfoService umsUserInfoService;

    @Autowired
    private UmsRoleService umsRoleService;

    @Override
    public int updateBatch(List<UmsUser> list) {
        return baseMapper.updateBatch(list);
    }

    @Override
    public int updateBatchSelective(List<UmsUser> list) {
        return baseMapper.updateBatchSelective(list);
    }
    @Override
    public int batchInsert(List<UmsUser> list) {
        return baseMapper.batchInsert(list);
    }
    @Override
    public int batchInsertSelectiveUseDefaultForNull(List<UmsUser> list) {
        return baseMapper.batchInsertSelectiveUseDefaultForNull(list);
    }
    @Override
    public int deleteByPrimaryKeyIn(List<Long> list) {
        return baseMapper.deleteByPrimaryKeyIn(list);
    }
//    @Override
//    public int insertOrUpdate(UmsUser record) {
//        return baseMapper.insertOrUpdate(record);
//    }
    @Override
    public int insertOrUpdateSelective(UmsUser record) {
        return baseMapper.insertOrUpdateSelective(record);
    }

    @Override
    public UmsUser selectByPrimaryKey(Integer id) {
        return this.baseMapper.selectById(id);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUmsUser(UmsSaveUserVo umsSaveUserVo) {
        //加密密碼
//        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
//        String encode = bCryptPasswordEncoder.encode(umsSaveUserVo.getPassword());
//        umsSaveUserVo.setPassword(encode);
//        log.info("加密過後密碼:{}",encode);

//        Role roleId = umsSaveUserVo.getRoleId();
//        log.info("roleId:{}",roleId);

        UmsUser umsUser = new UmsUser();//缺少avatar
        UmsUserInfo userInfo = new UmsUserInfo();

        //獲取user與userInfo主鍵
        long user_id = IdWorker.getId(umsUser);
        long userInfo_id = IdWorker.getId(userInfo);
        log.info("user_id:{}", user_id);
        log.info("userInfo_id:{}", userInfo_id);

        //設置user資料
        ///TODO 缺少Avatar屬性
        umsUser.setId(user_id);
        umsUser.setUserinfoId(userInfo_id);
        ///TODO 判斷是否有權限設定RoleId
        umsUser.setRoleId(umsSaveUserVo.getRoleId());//RoleName經過mybatisPlus枚舉轉換器映射成資料庫中roleId需要的類型
        BeanUtils.copyProperties(umsSaveUserVo, umsUser,"roleId");

        //設置userInfo資料
        userInfo.setId(userInfo_id);
        userInfo.setUserId(user_id);

        //處理性別屬性,將"男"轉為1,"女"轉為2,"不願透露"轉為3,方便保存
//        String gender = umsSaveUserVo.getGender();
//        switch (gender) {
//            case "男":
//                gender="1";
//                break;
//            case "女":
//                gender="2";
//                break;
//            case "不願透露":
//                gender="3";
//                break;
//        }

        userInfo.setGender(umsSaveUserVo.getGender());

        BeanUtils.copyProperties(umsSaveUserVo, userInfo,"gender");

        umsUserInfoService.saveUmsUserInfo(userInfo);
        this.baseMapper.insert(umsUser);


    }

    @Override
    public List<UmsUserDetailsDTO> listUmsUserDetails() {
        //獲取所有用戶
        List<UmsUser> umsUserList = listUmsUser();
        log.info("umsUserList:{}", umsUserList);
        //獲取所有用戶資訊
        List<UmsUserInfo> umsUserInfoList = umsUserInfoService.listUmsUserInfo();

        log.info("umsUserInfoList:{}", umsUserInfoList);
        //獲取所有權限
        List<UmsRole> umsRoleList = umsRoleService.list();
        log.info("umsumsRoleList:{}",umsRoleList);
        List<UmsUserDetailsDTO> umsUserDetailsDTOList = umsUserList.stream().map(umsUser -> {
            //將資料封裝進UmsUserDetailsDTO
            UmsUserDetailsDTO umsUserDetailsDTO = new UmsUserDetailsDTO();

            BeanUtils.copyProperties(umsUser, umsUserDetailsDTO);
            umsUserDetailsDTO.setRoleId(umsUser.getRoleId());
            umsUserInfoList.stream()
                    .filter(item-> Objects.equals(item.getUserId(), umsUser.getId()))
                    .findFirst()//取一個
                    .ifPresent(item-> {
                        BeanUtils.copyProperties(item, umsUserDetailsDTO,"id");
                        umsUserDetailsDTO.setUserInfoId(item.getId());
//                        Integer gender = item.getGender();
//                        switch (gender) {
//                            case 1:
//                                umsUserDetailsDTO.setGender("男");
//                                break;
//                            case 2:
//                                umsUserDetailsDTO.setGender("女");
//                                break;
//                            case 3:
//                                umsUserDetailsDTO.setGender("不願透露");
//                                break;
//                            default:
//                                umsUserDetailsDTO.setGender("出現異常");
//                                break;
//                        }
                    });//若存在則拷貝資料至DTO中

//            if (umsUserInfo != null) {
//                BeanUtils.copyProperties(umsUserInfo, umsUserDetailsDTO);
//            }

            umsRoleList.stream().filter(item -> Objects.equals(item.getId(), umsUser.getRoleId()))
                    .findFirst()//取一個
                    .ifPresent(item->BeanUtils.copyProperties(item,umsUserDetailsDTO,"id"));//若存在則拷貝資料至DTO中

//            if (umsRole != null) {
//                BeanUtils.copyProperties(umsRole, umsUserDetailsDTO);
//
            log.info("umsUserDetailsDTO:{}", umsUserDetailsDTO);
            return umsUserDetailsDTO;
        }).toList();

        log.info("umsUserDetailsDTOList:{}", umsUserDetailsDTOList );
        return umsUserDetailsDTOList;
    }



    @Override
    public List<UmsUser> listUmsUser() {
        return this.baseMapper.selectList(new LambdaQueryWrapper<>());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteUmsUserDetails(List<Long> userIdList) {

        List<UmsUser> umsUserList = this.baseMapper.selectBatchIds(userIdList);
        //利用用戶ID查出用戶資訊ID
        List<Long> userinfoIdList = umsUserList.stream().map(UmsUser::getUserinfoId).toList();

        //刪除User資料
        this.baseMapper.deleteByIds(userIdList);
        // 刪除UserInfo資料
        umsUserInfoService.removeByIds(userinfoIdList);
    }

    @Override
    public void updateUmsUserDetails(UmsUpdateUserDetailsVO updateUserDetailsVO, Long userId, Long userInfoId) {
        if(!updateUserDetailsVO.isEmpty()){//若有需要修改的屬性
            UmsUser user = new UmsUser();
            UmsUserInfo userInfo = new UmsUserInfo();
            BeanUtils.copyProperties(updateUserDetailsVO, user);
            BeanUtils.copyProperties(updateUserDetailsVO, userInfo);

            if(!user.isEmpty()){//若user中有需要修改屬性
                user.setId(userId);
                this.baseMapper.updateById(user);
            }
            if(!userInfo.isEmpty()){//若userInfo中有需要修改屬性

                ///TODO 考慮密碼安全性問題
//                String password = userInfo.getPassword();
//                if(StringUtils.hasText(password)){//若需修改密碼
//                    umsUserInfoService.passwordMatchesDatabase(userInfoId,password);
//                }

                userInfo.setId(userInfoId);
                umsUserInfoService.updateUserInfo(userInfo);
            }


        }else {
            throw new RuntimeException("未接受到任何需要修改的數據");
        }

//        Field[] userInfoFields = userInfo.getClass().getDeclaredFields();
//        ArrayList<Object> userInfoValues = new ArrayList<>();
//
//        for (Field userInfoField : userInfoFields) {
//            userInfoField.setAccessible(true);  // 設置可訪問私有屬性
//
//            Object userInfoValue = null;  // 獲取屬性的值
//            try {
//                userInfoValue = userInfoField.get(userInfo);
//
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//
//            if (userInfoValue == null) {
//                System.out.println(userInfoField.getName() + " is null");
//            } else {
//                System.out.println(userInfoField.getName() + " is not null, value: " + userInfoValue);
//                userInfoValues.add(userInfoValue);
//            }
//
//        }
//
//        ArrayList<Object> userValues = new ArrayList<>();
//        Field[] userFields = umsUser.getClass().getDeclaredFields();
//        for (Field userField : userFields) {
//            userField.setAccessible(true);  // 設置可訪問私有屬性
//
//            Object userValue = null;  // 獲取屬性的值
//            try {
//
//                userValue = userField.get(umsUser);
//            } catch (IllegalAccessException e) {
//                throw new RuntimeException(e);
//            }
//
//            if (userValue == null) {
//                System.out.println(userField.getName() + " is null");
//            } else {
//                System.out.println(userField.getName() + " is not null, value: " + userValue);
//                userValues.add(userValue);
//            }
//        }
//
////        log.info("fields:{}", fields);
//        log.info("userInfoValues:{}", userInfoValues);
//        log.info("userValues:{}", userValues);
//        System.out.println();

    }

    @Override
    public void updateUmsUserAvatar(String imgUrl, String userId) {
        UmsUser umsUser = this.baseMapper.selectById(userId);
        if(!umsUser.isEmpty()){
            umsUser.setAvatar(imgUrl);
            this.baseMapper.updateById(umsUser);
        }


    }


}
