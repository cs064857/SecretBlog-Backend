package com.shijiawei.secretblog.user.service.Impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.exception.CustomBaseException;
import com.shijiawei.secretblog.user.entity.*;
import com.shijiawei.secretblog.user.service.*;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.common.codeEnum.HttpCodeEnum;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.RedisRateLimiterUtils;
import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.enumValue.Role;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

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

    @Value("${user.default-avatar}")
    private String defaultAvatar;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedisRateLimiterUtils redisRateLimiterUtils;

    @Autowired
    private UmsCredentialsService umsCredentialsService;

    @Autowired
    private UmsAuthsService umsAuthsService;

    @Override
    public R userLogin(UmsUserLoginDTO umsUserLoginDTO) {
        return null;
    }

//    @Override
//    public int updateBatch(List<UmsUser> list) {
//        return baseMapper.updateBatch(list);
//    }
//
//    @Override
//    public int updateBatchSelective(List<UmsUser> list) {
//        return baseMapper.updateBatchSelective(list);
//    }
//    @Override
//    public int batchInsert(List<UmsUser> list) {
//        return baseMapper.batchInsert(list);
//    }
//    @Override
//    public int batchInsertSelectiveUseDefaultForNull(List<UmsUser> list) {
//        return baseMapper.batchInsertSelectiveUseDefaultForNull(list);
//    }
//    @Override
//    public int deleteByPrimaryKeyIn(List<Long> list) {
//        return baseMapper.deleteByPrimaryKeyIn(list);
//    }
//
//    @Override
//    public int insertOrUpdateSelective(UmsUser record) {
//        return baseMapper.insertOrUpdateSelective(record);
//    }



//    @Override
//    public int insertOrUpdate(UmsUser record) {
//        return baseMapper.insertOrUpdate(record);
//    }


    @Override
    public UmsUser selectByPrimaryKey(Long id) {
        return this.baseMapper.selectById(id);
    }

    /**
     * 管理員新增用戶接口
     * @param umsSaveUserVo
     */
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


        userInfo.setGender(umsSaveUserVo.getGender());

        BeanUtils.copyProperties(umsSaveUserVo, userInfo,"gender");
        log.info("umsUserInfo:{}",userInfo);
        umsUserInfoService.saveUmsUserInfo(userInfo);

        log.info("umsUser:{}",umsUser);
        this.baseMapper.insert(umsUser);


        UmsCredentials umsCredentials = new UmsCredentials();
        BeanUtils.copyProperties(umsSaveUserVo,umsCredentials);
        umsCredentials.setUserId(user_id);
        log.info("umsCredentials:{}",umsCredentials);
        umsCredentialsService.save(umsCredentials);

        UmsAuths umsAuths = new UmsAuths();
        umsAuths.setPassword(umsSaveUserVo.getPassword());
        umsAuths.setUserId(user_id);

        log.info("umsAuths:{}",umsAuths);
        umsAuthsService.save(umsAuths);

    }

    @Override
    public List<UmsUserDetailsDTO> listUmsUserDetails() {
        ///TODO 20250815 優化:將方式改成利用Map或者直接利用Mapper SQL聯合查詢

        //獲取所有用戶
        List<UmsUser> umsUserList = listUmsUser();
        log.info("umsUserList:{}", umsUserList);
        //獲取所有用戶資訊

        List<UmsUserInfo> umsUserInfoList = umsUserInfoService.listUmsUserInfo();


        List<UmsAuths> umsAuthsList = umsAuthsService.list();
        List<UmsCredentials> umsCredentialsList = umsCredentialsService.list();


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
            umsAuthsList.stream().filter(item ->Objects.equals(item.getUserId(),umsUser.getId())).findFirst().ifPresent(item->BeanUtils.copyProperties(item,umsUserDetailsDTO,"id"));
            umsCredentialsList.stream().filter(item ->Objects.equals(item.getUserId(),umsUser.getId())).findFirst().ifPresent(item->BeanUtils.copyProperties(item,umsUserDetailsDTO,"id"));
            log.info("umsUserDetailsDTO:{}", umsUserDetailsDTO);
            return umsUserDetailsDTO;
        }).toList();

        log.info("umsUserDetailsDTOList:{}", umsUserDetailsDTOList );
        return umsUserDetailsDTOList;
    }



    @Override
    public List<UmsUser> listUmsUser() {

        return this.baseMapper.selectList(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getDeleted,0));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R deleteUserDetailsByIds(List<Long> userIdList) {

//        List<UmsUser> umsUserList = this.baseMapper.selectBatchIds(userIdList);
        //利用用戶ID查出用戶資訊ID


//        List<Long> userinfoIdList = umsUserList.stream().map(UmsUser::getUserinfoId).toList();

        //刪除User資料,將deleted欄位改成1
        int deleted = this.baseMapper.deleteByIds(userIdList);
        if(deleted==0){
            throw new CustomBaseException("500","刪除失敗", HttpStatus.BAD_REQUEST);
        }
        // 刪除UserInfo資料
//        umsUserInfoService.removeByIds(userinfoIdList);
        // 刪除Auths資料
//        umsAuthsService.remove(new LambdaQueryWrapper<UmsAuths>().in(UmsAuths::getUserId, userIdList));
        // 刪除Credentials資料
//        umsCredentialsService.remove(new LambdaQueryWrapper<UmsCredentials>().in(UmsCredentials::getUserId, userIdList));

        return R.ok();
    }

    @Override
    public void updateUmsUserDetails(UmsUpdateUserDetailsVO updateUserDetailsVO, Long userId) {
        if(!updateUserDetailsVO.isEmpty()){//若有需要修改的屬性
            UmsUser user = new UmsUser();
            UmsUserInfo userInfo = new UmsUserInfo();
            UmsCredentials umsCredentials = new UmsCredentials();
            BeanUtils.copyProperties(updateUserDetailsVO, user);
            BeanUtils.copyProperties(updateUserDetailsVO, userInfo);
            BeanUtils.copyProperties(updateUserDetailsVO, umsCredentials);

            if(!user.isEmpty()){//若user中有需要修改屬性
                user.setId(userId);
                user.setUpdateAt(LocalDateTime.now());
                this.baseMapper.updateById(user);
            }
            if(!userInfo.isEmpty()){//若user中有需要修改屬性
                userInfo.setId(userId);
                userInfo.setUpdateAt(LocalDateTime.now());
//                umsUserInfoService.updateById(userInfo);
                umsUserInfoService.update(userInfo,new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getUserId, userId));
            }

            if(!umsCredentials.isEmpty()){//若userInfo中有需要修改屬性
                umsCredentials.setUserId(userId);
                umsCredentials.setUpdateAt(LocalDateTime.now());
                umsCredentialsService.update(umsCredentials,new LambdaQueryWrapper<UmsCredentials>().eq(UmsCredentials::getUserId, userId));
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

    /**
     * 用戶註冊街口
     * @param umsUserRegisterDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R UmsUserRegister(UmsUserRegisterDTO umsUserRegisterDTO) {

        UmsUser umsUser = new UmsUser();
        UmsUserInfo umsUserInfo = new UmsUserInfo();



        /// TODO 在儲存用戶密碼時加密




        BeanUtils.copyProperties(umsUserRegisterDTO, umsUser);
        BeanUtils.copyProperties(umsUserRegisterDTO, umsUserInfo);

        //獲取user與userInfo主鍵
        long user_id = IdWorker.getId(umsUser);
        long userInfo_id = IdWorker.getId(umsUserInfo);



        //設置user資料
        umsUser.setId(user_id);
        umsUser.setUserinfoId(userInfo_id);
        umsUser.setRoleId(Role.NORMALUSER);//設置角色為普通用戶
        umsUser.setAvatar(defaultAvatar);//設置默認頭像
        //設置userInfo資料
        umsUserInfo.setId(userInfo_id);
        umsUserInfo.setUserId(user_id);

        
        ///TODO 確認信箱驗證碼
        ///TODO 再次判斷用戶是否已註冊
        //獲得用戶輸入的驗證碼
        String vaildCode = umsUserRegisterDTO.getEmailValidCode();
        //獲得用戶的信箱
        String email = umsUserRegisterDTO.getEmail();
        //從Redis中取得驗證碼並校驗，桶名為umsuser:validcode:abcd@gmail.com:
        String bucket = "umsuser:validcode_"+email;
        String validCodeFromRedis = (String) redissonClient.getBucket(bucket).get();
        //判斷驗證碼是否正確
        if(vaildCode.equals(validCodeFromRedis)){

            //將用戶資料插入資料庫
            baseMapper.insert(umsUser);
            umsUserInfoService.saveUmsUserInfo(umsUserInfo);
            UmsAuths umsAuths = new UmsAuths();
            umsAuths.setUserId(user_id);
            umsAuths.setPassword(umsUserRegisterDTO.getPassword());
            log.info("umsAuths:{}",umsAuths);
            umsAuthsService.save(umsAuths);

            UmsCredentials umsCredentials = new UmsCredentials();
            umsCredentials.setUserId(user_id);
            umsCredentials.setEmail(umsUserRegisterDTO.getEmail());
            log.info("umsCredentials:{}",umsCredentials);
            umsCredentialsService.save(umsCredentials);

            //將Redis中保存的驗證碼刪除
            redissonClient.getBucket(bucket).deleteAsync();
            return R.ok();
        }else {
            //回傳驗證碼錯誤
            return new R(HttpCodeEnum.CAPTCHA_ERR.getCode(), HttpCodeEnum.CAPTCHA_ERR.getDescription());
        }

    }


    @Override
    public R sendVerificationCode(UmsUserEmailVerifyDTO umsUserEmailVerifyDTO) {

        //根據IP判斷進行限流,短時間內嘗試過多次則暫時禁止
        String remoteAddr = httpServletRequest.getRemoteAddr();
        log.info("remoteAddr：{}",remoteAddr);
        /*
         * Redis限流
         */
        //Redis中IP嘗試計數的桶名
        String rateLimitBucket = "umsuser:validcode_ratelimit_ipaddr_"+remoteAddr;

//
        ///TODO 調整驗證碼嘗試限流時間，目前30秒3次
//        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitBucket);
//        rateLimiter.trySetRate(RateType.OVERALL,3,30, RateIntervalUnit.SECONDS);
        RRateLimiter rateLimiter = redisRateLimiterUtils.setRedisRateLimiter(rateLimitBucket, RateType.PER_CLIENT, 3, 30, RateIntervalUnit.SECONDS);

//            String accountName = umsUserEmailVerifyDTO.getAccountName();
        String email = umsUserEmailVerifyDTO.getEmail();

        long emailIsExists = umsCredentialsService.count(new LambdaQueryWrapper<UmsCredentials>().eq(UmsCredentials::getEmail, email));
        if(emailIsExists>0){
            /// TODO 修改拋出異常的具體參數
            throw new CustomBaseException("此電子郵件地址已被註冊");
        }

//        umsUserInfoService.getOne(new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getAccountName,))

        // 嘗試獲取許可
        if (redisRateLimiterUtils.tryAcquire(rateLimiter)) {
            /*
              成功獲取許可，執行業務邏輯
             */

            Random random = new Random();
            String VaildCodeString = String.format("%06d", random.nextInt(90000) + 10000);
            ///TODO 取消顯示驗證碼
            log.info("驗證碼：{}", VaildCodeString);

            //將驗證碼保存在Redis中，設置過期時間為15分鐘，桶名為umsuser:validcode:abcd@gmail.com:
            String bucket = "umsuser:validcode_"+email;
            //Redis快取中儲存新的驗證碼
            redissonClient.getBucket(bucket).set(VaildCodeString, Duration.of(15,ChronoUnit.MINUTES));
            ///TODO 發送驗證碼到用戶的信箱中
            return R.ok("驗證碼已發送至您的郵箱");
        } else {
            // 超過請求限流，拒絕請求
            return new R(HttpCodeEnum.TOO_MANY_REQUESTS.getCode(), HttpCodeEnum.TOO_MANY_REQUESTS.getDescription());
        }
    }

    @Override
    public List<UserBasicDTO> selectUserBasicInfoByIds(List<Long> ids) {
        return this.baseMapper.selectUserBasicInfoByIds(ids);
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R deleteUserDetailsById(Long userId) {
        //將user的deleted欄位設置為1(邏輯刪除)
        int deleted = this.baseMapper.deleteById(userId);
        if(deleted == 0){
            throw new CustomBaseException("500","刪除失敗", HttpStatus.BAD_REQUEST);
        }
//        // 刪除使用者資訊
//        if (!umsUserInfoService.remove(new LambdaQueryWrapper<UmsUserInfo>()
//                .eq(UmsUserInfo::getUserId, userId))) {
//            throw new CustomBaseException("500", "刪除使用者資訊失敗", HttpStatus.BAD_REQUEST);
//        }
//        // 刪除授權資料
//        if (!umsAuthsService.remove(new LambdaQueryWrapper<UmsAuths>()
//                .eq(UmsAuths::getUserId, userId))) {
//            throw new CustomBaseException("500", "刪除授權資訊失敗", HttpStatus.BAD_REQUEST);
//        }
//        // 刪除憑證資料
//        if (!umsCredentialsService.remove(new LambdaQueryWrapper<UmsCredentials>()
//                .eq(UmsCredentials::getUserId, userId))) {
//            throw new CustomBaseException("500", "刪除憑證資料失敗", HttpStatus.BAD_REQUEST);
//        }
        return R.ok();

    }


}
