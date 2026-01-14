package com.shijiawei.secretblog.user.service.Impl;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import com.pig4cloud.captcha.SpecCaptcha;
import com.pig4cloud.captcha.utils.CaptchaUtil;
import com.shijiawei.secretblog.user.vo.UmsSaveUserVo;
import com.shijiawei.secretblog.user.vo.UmsUpdateUserDetailsVO;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.feign.dto.UmsUserAvatarUpdateDTO;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import com.shijiawei.secretblog.user.entity.*;
import com.shijiawei.secretblog.user.feign.ArticleFeignClient;
import com.shijiawei.secretblog.user.service.*;
import com.shijiawei.secretblog.user.DTO.UmsChangePasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsForgotPasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsResetPasswordDTO;
import com.shijiawei.secretblog.user.DTO.UmsVerifyResetTokenDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.common.codeEnum.HttpCodeEnum;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.redis.RedisRateLimiterUtils;

import com.shijiawei.secretblog.user.DTO.UmsUserDetailsDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserEmailVerifyDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserRegisterDTO;
import com.shijiawei.secretblog.user.DTO.UmsUserSummaryDTO;
import com.shijiawei.secretblog.common.enumValue.Role;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
import com.shijiawei.secretblog.common.utils.AvatarUrlHelper;

import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Value("${custom.front-domain}")
    private String frontendBaseUrl;

    @Value("${custom.minio-domain}")
    private String minioDomain;

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

    @Autowired
    private ArticleFeignClient articleFeignClient;

    @Autowired
    private UmsLocalMessageService umsLocalMessageService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
        UmsUser user = this.baseMapper.selectById(id);
        if (user != null) {
            user.setAvatar(AvatarUrlHelper.toPublicUrl(user.getAvatar(), minioDomain));
        }
        return user;
    }

    /**
     * 管理員新增用戶接口
     * @param umsSaveUserVo
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveUmsUser(UmsSaveUserVo umsSaveUserVo) {
        //加密密碼
        String encode = passwordEncoder.encode(umsSaveUserVo.getPassword());
        umsSaveUserVo.setPassword(encode);
        log.info("加密過後密碼:{}",encode);

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
        umsUser.setAvatar(AvatarUrlHelper.toStoragePath(defaultAvatar, minioDomain));

        //設置userInfo資料
        userInfo.setId(userInfo_id);
        userInfo.setUserId(user_id);
        userInfo.setNotifyEnabled((byte) 1);


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
            umsUserDetailsDTO.setAvatar(AvatarUrlHelper.toPublicUrl(umsUserDetailsDTO.getAvatar(), minioDomain));
            log.info("umsUserDetailsDTO:{}", umsUserDetailsDTO);
            return umsUserDetailsDTO;
        }).toList();

        log.info("umsUserDetailsDTOList:{}", umsUserDetailsDTOList );

        //敏感資料脫敏處理
        umsUserDetailsDTOList.forEach(dto -> {
            //清除密碼
            dto.setPassword(null);
            //清除地址
            dto.setAddress(null);
            //手機號碼遮罩(中間5碼)
            String phone = dto.getPhoneNumber();
            if (phone != null && phone.length() >= 7) {
                String masked = phone.substring(0, 3) + "*****" + phone.substring(phone.length() - 2);
                dto.setPhoneNumber(masked);
            }
        });

        return umsUserDetailsDTOList;
    }



    @Override
    public List<UmsUser> listUmsUser() {

        List<UmsUser> users = this.baseMapper.selectList(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getDeleted,0));
        users.forEach(user -> user.setAvatar(AvatarUrlHelper.toPublicUrl(user.getAvatar(), minioDomain)));
        return users;
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
//            throw new CustomRuntimeException("500","刪除失敗");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.USER_INTERNAL_ERROR)
                    .detailMessage("未能獲得用戶基本資料")
                    .data(Map.of("userIds", ObjectUtils.defaultIfNull(userIdList,"")))
                    .build();
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
            updateUserDetailsVO.setAvatar(AvatarUrlHelper.toStoragePath(updateUserDetailsVO.getAvatar(), minioDomain));
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


        } else {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("未接受到任何需要修改的數據")
                    .data(Map.of(
                            "userId", Objects.requireNonNullElse(userId, -1L)
                    ))
                    .build();
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
    public R<Void> updateUmsUserAvatar(UmsUserAvatarUpdateDTO dto) {
        log.info("updateUmsUserAvatar imgUrl:{} userId:{}", dto.getAvatar(), dto.getUserId());

        String imgUrl = dto.getAvatar();
        Long userId = dto.getUserId();
        String storedAvatar = AvatarUrlHelper.toStoragePath(imgUrl, minioDomain);

        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .detailMessage("用戶id不存在")
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .data(Map.of("userId", ObjectUtils.defaultIfNull(userId, "")))
                    .build();
        }
        if (imgUrl == null) {
            throw BusinessRuntimeException.builder()
                    .detailMessage("用戶頭像不存在")
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .data(Map.of("avatar", ObjectUtils.defaultIfNull(imgUrl, ""),
                            "userId", ObjectUtils.defaultIfNull(userId, "")))
                    .build();
        }

        UmsUser umsUser = this.baseMapper.selectById(userId);
        if (umsUser != null && !umsUser.isEmpty()) {
            umsUser.setAvatar(storedAvatar);
            int update = this.baseMapper.updateById(umsUser);
            if (update > 0) {
                // 使用本地消息表記錄作者資訊更新消息，確保最終一致性
                String publicAvatar = AvatarUrlHelper.toPublicUrl(storedAvatar, minioDomain);
                AuthorInfoUpdateMessage authorInfoUpdateMessage = new AuthorInfoUpdateMessage(userId, publicAvatar, System.currentTimeMillis());


                umsLocalMessageService.createPendingMessage(authorInfoUpdateMessage);
                return R.ok();
            }
        }
        throw BusinessRuntimeException.builder()
                .detailMessage("用戶資料不存在")
                .iErrorCode(ResultCode.NOT_FOUND)
                .data(Map.of("userId", ObjectUtils.defaultIfNull(userId, "")))
                .build();
    }


    @Override
    public void updateNickname(Long userId, String nickName) {
        checkPermission(userId);
        UmsUser user = new UmsUser();
        user.setId(userId);
        user.setNickName(nickName);
        this.updateById(user);

        // 同步更新文章模組的作者資訊
        /// TODO: 使用 RabbitMQ 異步處理
        try {
            articleFeignClient.updateAuthorInfo(new ArticleFeignClient.AmsAuthorUpdateDTO(userId, nickName, null));
        } catch (Exception e) {
            log.error("Failed to sync nickname to article service", e);
        }
    }

    @Override
    public void updateGender(Long userId, Integer gender) {
        checkPermission(userId);
        LambdaUpdateWrapper<UmsUserInfo> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UmsUserInfo::getUserId, userId)
                .set(UmsUserInfo::getGender, gender);
        umsUserInfoService.update(updateWrapper);
    }

    /**
     * 用戶註冊接口
     * @param umsUserRegisterDTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R UmsUserRegister(UmsUserRegisterDTO umsUserRegisterDTO) {

        UmsUser umsUser = new UmsUser();
        UmsUserInfo umsUserInfo = new UmsUserInfo();

        // 儲存用戶密碼時加密
        umsUserRegisterDTO.setPassword(passwordEncoder.encode(umsUserRegisterDTO.getPassword()));

        BeanUtils.copyProperties(umsUserRegisterDTO, umsUser);
        BeanUtils.copyProperties(umsUserRegisterDTO, umsUserInfo);

        //獲取user與userInfo主鍵
        long user_id = IdWorker.getId(umsUser);
        long userInfo_id = IdWorker.getId(umsUserInfo);


        //設置user資料
        umsUser.setId(user_id);
        umsUser.setUserinfoId(userInfo_id);
        umsUser.setRoleId(Role.NORMALUSER);//設置角色為普通用戶
        umsUser.setAvatar(AvatarUrlHelper.toStoragePath(defaultAvatar, minioDomain));//設置默認頭像
        //設置userInfo資料
        umsUserInfo.setId(userInfo_id);
        umsUserInfo.setUserId(user_id);
        umsUserInfo.setNotifyEnabled((byte) 1);



        //獲得用戶輸入的驗證碼
        String vaildCode = umsUserRegisterDTO.getEmailValidCode();
        //獲得用戶的信箱
        String email = umsUserRegisterDTO.getEmail();

        // 設置預設帳號名稱 (Email 前綺)
        if (StringUtils.isBlank(umsUserInfo.getAccountName())) {
            String defaultAccountName = email.split("@")[0];
            umsUserInfo.setAccountName(defaultAccountName);
            log.info("為新用戶設置預設帳號名稱: {}", defaultAccountName);
        }

        // 再次判斷帳號/信箱是否已被占用（避免繞過寄送驗證碼流程）
        long emailIsExists = umsCredentialsService.count(new LambdaQueryWrapper<UmsCredentials>().eq(UmsCredentials::getEmail, email));
        if (emailIsExists > 0) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.USER_EMAIL_ALREADY_EXIST)
                    .data(Map.of("email", StringUtils.defaultString(email, "")))
                    .build();
        }

        String accountName = umsUserInfo.getAccountName();
        long accountNameIsExists = umsUserInfoService.count(new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getAccountName, accountName));
        if (accountNameIsExists > 0) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.USER_ACCOUNT_ALREADY_EXIST)
                    .data(Map.of("accountName", StringUtils.defaultString(accountName, "")))
                    .build();
        }

        // 從 Redis 取得驗證碼並校驗（Key 統一由 RedisCacheKey 管理）
        String bucket = RedisCacheKey.USER_EMAIL_VALID_CODE.format(email);
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
//        log.info("remoteAddr：{}",remoteAddr);


        /*
         * Redis限流
         */
        // Redis 中 IP 嘗試計數的 Key（寄送驗證碼限流）
        String rateLimitBucket = RedisCacheKey.USER_EMAIL_VALID_CODE_RATE_LIMIT_IP.format(remoteAddr);


        ///TODO 調整驗證碼嘗試限流時間，目前30秒3次
//        RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimitBucket);
//        rateLimiter.trySetRate(RateType.OVERALL,3,30, RateIntervalUnit.SECONDS);
        RRateLimiter rateLimiter = redisRateLimiterUtils.setRedisRateLimiter(rateLimitBucket, RateType.PER_CLIENT, 3, 30, RateIntervalUnit.SECONDS);

        String email = umsUserEmailVerifyDTO.getEmail();
        String accountName = StringUtils.trimToNull(umsUserEmailVerifyDTO.getAccountName());
        if (accountName == null && StringUtils.isNotBlank(email) && email.contains("@")) {
            accountName = email.split("@")[0];
        }

        long emailIsExists = umsCredentialsService.count(new LambdaQueryWrapper<UmsCredentials>().eq(UmsCredentials::getEmail, email));
        if(emailIsExists>0){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.USER_EMAIL_ALREADY_EXIST)
                    .data(Map.of("email", StringUtils.defaultString(email,"")))
                    .build();
        }

        if (StringUtils.isNotBlank(accountName)) {
            long accountNameIsExists = umsUserInfoService.count(new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getAccountName, accountName));
            if (accountNameIsExists > 0) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.USER_ACCOUNT_ALREADY_EXIST)
                        .data(Map.of("accountName", StringUtils.defaultString(accountName, "")))
                        .build();
            }
        }

        // 嘗試獲取許可
        if (redisRateLimiterUtils.tryAcquire(rateLimiter)) {
            /*
              成功獲取許可，執行業務邏輯
             */

            // 1. 校驗圖形驗證碼
            String captchaKey = umsUserEmailVerifyDTO.getCaptchaKey();
            String captchaCode = umsUserEmailVerifyDTO.getCaptchaCode();
            String captchaBucketKey = RedisCacheKey.USER_CAPTCHA.format(captchaKey);
            String captchaInRedis = (String) redissonClient.getBucket(captchaBucketKey).get();

            if (StringUtils.isBlank(captchaInRedis) || !captchaInRedis.equalsIgnoreCase(captchaCode)) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.CAPTCHA_INVALID)
                        .detailMessage("圖形驗證碼校驗失敗")
                        .data(Map.of(
                                "captchaKey", StringUtils.defaultString(captchaKey, ""),
                                "captchaExists", StringUtils.isNotBlank(captchaInRedis)
                        ))
                        .build();
            }

            // 2. 校驗成功後立即刪除驗證碼，防止重用
            redissonClient.getBucket(captchaBucketKey).deleteAsync();

            Random random = new Random();
            String VaildCodeString = String.format("%06d", random.nextInt(90000) + 10000);
//            log.debug("驗證碼：{}", VaildCodeString);

            // 將驗證碼保存在 Redis 中，設置過期時間為 15 分鐘（Key 統一由 RedisCacheKey 管理）
            String bucket = RedisCacheKey.USER_EMAIL_VALID_CODE.format(email);
            //Redis快取中儲存新的驗證碼
            redissonClient.getBucket(bucket).set(VaildCodeString, Duration.of(15,ChronoUnit.MINUTES));

            // 發送驗證碼到用戶的信箱中
            try {
                emailService.sendVerificationCodeEmail(email, VaildCodeString);
            } catch (Exception e) {
                log.error("郵件發送失敗: {}", e.getMessage());
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.USER_INTERNAL_ERROR)
                        .detailMessage("郵件發送失敗，請稍後再試")
                        .data(Map.of("email", StringUtils.defaultString(email, "")))
                        .build();
            }

            return R.ok("驗證碼已發送至您的郵箱");
        } else {
            // 超過請求限流，改以例外回應
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.TOO_MANY_REQUESTS)
                    .detailMessage("寄送信箱驗證碼觸發限流")
                    .data(Map.of(
                            "ip", StringUtils.defaultString(remoteAddr, ""),
                            "email", StringUtils.defaultString(email, "")
                    ))
                    .build();
        }
    }

    @Override
    public List<UserBasicDTO> selectUserBasicInfoByIds(List<Long> ids) {
        List<UserBasicDTO> userBasicDTOS = this.baseMapper.selectUserBasicInfoByIds(ids);
        userBasicDTOS.forEach(dto -> dto.setAvatar(AvatarUrlHelper.toPublicUrl(dto.getAvatar(), minioDomain)));
        return userBasicDTOS;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public R deleteUserDetailsById(Long userId) {
        //將user的deleted欄位設置為1(邏輯刪除)
        int deleted = this.baseMapper.deleteById(userId);
        if(deleted == 0){
//            throw new CustomRuntimeException("500","刪除失敗");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.DELETE_FAILED)
                    .detailMessage("刪除用戶失敗")
                    .data(Map.of("userId", ObjectUtils.defaultIfNull(userId,"")))
                    .build();
        }
//        // 刪除使用者資訊
//        if (!umsUserInfoService.remove(new LambdaQueryWrapper<UmsUserInfo>()
//                .eq(UmsUserInfo::getUserId, userId))) {
//            throw new CustomRuntimeException("500", "刪除使用者資訊失敗", HttpStatus.BAD_REQUEST);
//        }
//        // 刪除授權資料
//        if (!umsAuthsService.remove(new LambdaQueryWrapper<UmsAuths>()
//                .eq(UmsAuths::getUserId, userId))) {
//            throw new CustomRuntimeException("500", "刪除授權資訊失敗", HttpStatus.BAD_REQUEST);
//        }
//        // 刪除憑證資料
//        if (!umsCredentialsService.remove(new LambdaQueryWrapper<UmsCredentials>()
//                .eq(UmsCredentials::getUserId, userId))) {
//            throw new CustomRuntimeException("500", "刪除憑證資料失敗", HttpStatus.BAD_REQUEST);
//        }
        return R.ok();

    }

    @Override
    public UmsUserSummaryDTO getUserSummary(Long id) {
        UmsUserSummaryDTO dto = this.baseMapper.selectUserSummaryById(id);
        if (dto != null) {
            dto.setAvatar(AvatarUrlHelper.toPublicUrl(dto.getAvatar(), minioDomain));
        }
        return dto;
    }

    private void checkPermission(Long userId) {
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入")
                    .build();
        }
        Long currentUserId = UserContextHolder.getCurrentUserId();
        if (!userId.equals(currentUserId) && !UserContextHolder.isCurrentUserAdmin()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.FORBIDDEN)
                    .detailMessage("無權修改該用戶資料")
                    .build();
        }
    }

    @Override
    public R changePassword(UmsChangePasswordDTO dto) {
        // 從 UserContextHolder 獲取當前用戶 ID
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();

        // 驗證新密碼和確認密碼是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("新密碼與確認密碼不一致")
                    .build();
        }

        // 根據 userId 查詢用戶的認證資訊
        UmsAuths umsAuths = umsAuthsService.getOne(
                new LambdaQueryWrapper<UmsAuths>().eq(UmsAuths::getUserId, userId)
        );

        if (umsAuths == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶認證資訊不存在")
                    .data(Map.of("userId", ObjectUtils.defaultIfNull(userId, "")))
                    .build();
        }

        // 驗證舊密碼是否正確
        if (!passwordEncoder.matches(dto.getOldPassword(), umsAuths.getPassword())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("舊密碼不正確")
                    .build();
        }

        // 更新密碼
        umsAuths.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        umsAuths.setPasswordUpdatedAt(LocalDateTime.now());
        umsAuths.setUpdateAt(LocalDateTime.now());
        umsAuthsService.updateById(umsAuths);

        log.info("用戶 {} 已成功修改密碼", userId);
        return R.ok("密碼修改成功");
    }

    @Override
    public R sendForgotPasswordCode(UmsForgotPasswordDTO dto) {
        String email = dto.getEmail();

        // 根據 IP 進行限流
        String remoteAddr = httpServletRequest.getRemoteAddr();
        String rateLimitBucket = RedisCacheKey.USER_FORGOT_PASSWORD_RATE_LIMIT_IP.format(remoteAddr);
        RRateLimiter rateLimiter = redisRateLimiterUtils.setRedisRateLimiter(
                rateLimitBucket, RateType.PER_CLIENT, 3, 60, RateIntervalUnit.SECONDS
        );
        // 設置過期時間, 避免永久存在導致占用Redis
        rateLimiter.expire(Duration.of(5, ChronoUnit.MINUTES));

        // 檢查 Email 是否存在
        UmsCredentials credentials = umsCredentialsService.getOne(
                new LambdaQueryWrapper<UmsCredentials>().eq(UmsCredentials::getEmail, email)
        );

        if (credentials == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("該電子郵件地址未註冊")
                    .data(Map.of("email", StringUtils.defaultString(email, "")))
                    .build();
        }

        if (redisRateLimiterUtils.tryAcquire(rateLimiter)) {
            // 生成 UUID Token
            String token = java.util.UUID.randomUUID().toString();
            log.info("密碼重設Token已生成，Email: {}", email);

            // 將 Token -> UserId 映射保存在 Redis 中，過期時間 30 分鐘
            String bucket = RedisCacheKey.USER_PASSWORD_RESET_TOKEN.format(token);
            redissonClient.getBucket(bucket).set(credentials.getUserId(), Duration.of(30, ChronoUnit.MINUTES));

            // 構建重設密碼 URL
            String resetUrl = frontendBaseUrl + "/reset-password?token=" + token;

            // 發送郵件
            try {
                emailService.sendPasswordResetEmail(email, resetUrl);
            } catch (Exception e) {
                log.error("郵件發送失敗: {}", e.getMessage());
                // 刪除已儲存的 Token
                redissonClient.getBucket(bucket).deleteAsync();
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.USER_INTERNAL_ERROR)
                        .detailMessage("郵件發送失敗，請稍後再試")
                        .data(Map.of("email", StringUtils.defaultString(email, "")))
                        .build();
            }

            return R.ok("密碼重設連結已發送至您的郵箱");
        } else {
            return new R(HttpCodeEnum.TOO_MANY_REQUESTS.getCode(), HttpCodeEnum.TOO_MANY_REQUESTS.getDescription());
        }
    }

    @Override
    public R verifyResetToken(String token) {
        if (StringUtils.isBlank(token)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("Token 不能為空")
                    .build();
        }

        String bucket = RedisCacheKey.USER_PASSWORD_RESET_TOKEN.format(token);
        Long userId = (Long) redissonClient.getBucket(bucket).get();

        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("連結已過期或無效，請重新申請密碼重設")
                    .build();
        }

        return R.ok("Token 有效");
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public R resetPassword(UmsResetPasswordDTO dto) {
        String token = dto.getToken();

        // 驗證新密碼和確認密碼是否一致
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("新密碼與確認密碼不一致")
                    .build();
        }

        // 從 Redis 中獲取 Token 對應的 UserId
        String bucket = RedisCacheKey.USER_PASSWORD_RESET_TOKEN.format(token);
        Long userId = (Long) redissonClient.getBucket(bucket).get();

        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("連結已過期或無效，請重新申請密碼重設")
                    .build();
        }

        // 更新密碼
        UmsAuths umsAuths = umsAuthsService.getOne(
                new LambdaQueryWrapper<UmsAuths>().eq(UmsAuths::getUserId, userId)
        );

        if (umsAuths == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶認證資訊不存在")
                    .build();
        }

        umsAuths.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        umsAuths.setPasswordUpdatedAt(LocalDateTime.now());
        umsAuths.setUpdateAt(LocalDateTime.now());
        umsAuthsService.updateById(umsAuths);

        // 刪除 Redis 中的 Token（一次性使用）
        redissonClient.getBucket(bucket).deleteAsync();

        log.info("用戶 {} 已成功重設密碼", userId);
        return R.ok("密碼重設成功");
    }

    @Override
    public void updateNotifyEnabled(Long userId, Byte notifyEnabled) {
        checkPermission(userId);

        if (notifyEnabled == null || (notifyEnabled != 0 && notifyEnabled != 1)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("notifyEnabled 僅允許為 0 或 1")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "notifyEnabled", ObjectUtils.defaultIfNull(notifyEnabled, "")
                    ))
                    .build();
        }

        boolean updated = umsUserInfoService.update(new LambdaUpdateWrapper<UmsUserInfo>()
                .eq(UmsUserInfo::getUserId, userId)
                .set(UmsUserInfo::getNotifyEnabled, notifyEnabled)
                .set(UmsUserInfo::getUpdateAt, LocalDateTime.now())
        );

        if (!updated) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("更新通知總開關失敗")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "notifyEnabled", ObjectUtils.defaultIfNull(notifyEnabled, "")
                    ))
                    .build();
        }

        syncNotifyEnabledCache(userId, notifyEnabled);
    }

    @Override
    public R createCaptcha() {

        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 5);
        String verCode = specCaptcha.text().toLowerCase();
        String key = UUID.randomUUID().toString();
        // 存入redis並設置過期時間為30分鐘
        String cacheKey = RedisCacheKey.USER_CAPTCHA.format(key);
        redissonClient.getBucket(cacheKey).set(verCode, RedisCacheKey.USER_CAPTCHA.getTtl());

        // 將key和base64返回給前端
        return R.ok(Map.of("key", key, "base64", specCaptcha.toBase64()));
    }

    private void syncNotifyEnabledCache(Long userId, Byte notifyEnabled) {
        String cacheKey = RedisCacheKey.USER_NOTIFY_ENABLED.format(userId);
        try {
            Duration ttl = RedisCacheKey.USER_NOTIFY_ENABLED.getTtl();
            if (ttl != null) {
                redissonClient.getBucket(cacheKey).set(notifyEnabled.intValue(), ttl);
            } else {
                redissonClient.getBucket(cacheKey).set(notifyEnabled.intValue());
            }
        } catch (Exception e) {
            log.warn("同步通知總開關快取失敗，將依快取 TTL 自然過期或下次查詢回填，userId={}", userId, e);
        }
    }

}
