package com.shijiawei.secretblog.user.authentication.service;

import com.shijiawei.secretblog.user.entity.UmsAuths;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.service.UmsAuthsService;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.mapper.UmsUserInfoMapper;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
import org.springframework.util.StringUtils;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserService {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UmsUserMapper umsUserMapper;

  @Autowired
  private UmsUserInfoMapper umsUserInfoMapper;

  @Autowired
  private UmsAuthsService umsAuthsService;

  @Autowired
  private UmsCredentialsService umsCredentialsService;

  /**
   * 通過openId獲取用戶信息
   * @param openId
   * @param thirdPlatform 三方平台，比如gitee/qq/wechat
   * @return
   */
  public UmsUser getUserByOpenId(String openId, String thirdPlatform) {
    System.out.println("通過openId從數據庫查詢user"); // todo
    if (thirdPlatform.equals("gitee")) {
      UmsUser testUser = new UmsUser();
//      testUser.setUserId(1003L);
//      testUser.setNickName("Tom");
//      testUser.setRoleId("manager");
//      testUser.setPassword(passwordEncoder.encode("manager"));
//      testUser.setPhone("123000123");
      return testUser;
    }
    return null;
  }

  public UmsUser getUserByPhone(String phoneNumber) {
    if (phoneNumber.equals("1234567890")) {
      UmsUser testUser = new UmsUser();
//      testUser.setUserId(1002L);
//      testUser.setNickName("manager");
//      testUser.setRoleId("manager");
//      testUser.setPassword(passwordEncoder.encode("manager"));
//      testUser.setPhone("1234567890");
      return testUser;
    }
    return null;
  }

//  public UmsUserLoginDTO getUserFromDB(String nickName) {
//
//    UmsUserInfo umsUserInfo = umsUserInfoMapper.selectOne(new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getAccountName, nickName));
//
//    if(umsUserInfo!=null){
//      UmsUser umsUser = umsUserMapper.selectOne(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getUserinfoId, umsUserInfo.getId()));
//      if (nickName.equals(umsUserInfo.getAccountName())) {
////      if (nickName.equals("admin")) {
//        UmsUserLoginDTO testUser = new UmsUserLoginDTO();
//        testUser.setAccountName(umsUserInfo.getAccountName());
//        testUser.setDeleted(umsUser.getDeleted());
//        testUser.setEmail(umsUserInfo.getEmail());
//        testUser.setRoleId(umsUser.getRoleId());
//        testUser.setPassword(passwordEncoder.encode(umsUserInfo.getPassword()));
////      testUser.setUserId(1001L);
////      testUser.setNickName("admin");
////      testUser.setRoleId(Role.ADMIN);
////      testUser.setPassword(passwordEncoder.encode("admin"));
//        return testUser;
//      }
//    }
//
//
//
//
//    return null;
//  }

//  public UmsUserLoginDTO getUserFromDB(String usernameOrEmail) {
//    return Optional.ofNullable(
//
//            umsUserInfoMapper.selectOne(
//                    new LambdaQueryWrapper<UmsUserInfo>()
//                    .eq(UmsUserInfo::getAccountName,usernameOrEmail)
//            )
//    ).or(()->Optional.ofNullable(
//
//            umsUserInfoMapper.selectOne(
//                    new LambdaQueryWrapper<UmsUserInfo>()
//                    .eq(UmsUserInfo::getEmail, usernameOrEmail)
//            )
//
//    )).map(umsUserInfo -> {
//
//      UmsUser umsUser = umsUserMapper.selectOne(
//              new LambdaQueryWrapper<UmsUser>()
//                      .eq(UmsUser::getUserinfoId,umsUserInfo.getId()
//                      );
//      return Optional.ofNullable(umsUser)
//              .map(user-{)
//
//      )
//
//    })
//    return null;
//
//
//  }

//  public UmsUserLoginDTO getUserFromDB(String usernameOrEmail) {
//    // 使用 lambda 表達式結合 Optional 處理查詢邏輯
//    return Optional.ofNullable(
//                    // 以 accountName 查詢（DDL 變更後已無 email 欄位）
//                    umsUserInfoMapper.selectOne(
//                            new LambdaQueryWrapper<UmsUserInfo>()
//                                    .eq(UmsUserInfo::getAccountName, usernameOrEmail)
//                    )
//            )
//            .map(umsUserInfo -> {
//              // 查詢對應的 UmsUser
//              UmsUser umsUser = umsUserMapper.selectOne(
//                      new LambdaQueryWrapper<UmsUser>()
//                              .eq(UmsUser::getUserinfoId, umsUserInfo.getId())
//              );
//
//                UmsAuths umsAuthsServiceOne = umsAuthsService.getOne(new LambdaQueryWrapper<UmsAuths>().eq(UmsAuths::getUserId, umsUser.getId()));
//
//                UmsCredentials umsCredentialsServiceOne = umsCredentialsService.getOne(new LambdaQueryWrapper<UmsCredentials>().eq(UmsCredentials::getUserId, umsUser.getId()));
//
//
//                return Optional.ofNullable(umsUser)
//                        .flatMap(user->
//                                Optional.ofNullable(umsAuthsServiceOne))
//                                    .flatMap(auths ->
//                                            Optional.ofNullable(umsCredentialsServiceOne)
//                                                    .map(credentials->{
//
//                                                        return new UmsUserLoginDTO();
//                                                    })
//                                    )
//                        .orElse(null);
//
//
//
//
//              // 構建並返回 UmsUserLoginDTO
//
//            })
//            .orElse(null);
//  }

public Optional<UmsUserLoginDTO> getUserFromDB(String usernameOrEmail) {
    log.debug("[getUserFromDB] start usernameOrEmail='{}'", usernameOrEmail);
    long t0 = System.currentTimeMillis();
    Optional<UmsUserLoginDTO> result = Optional.ofNullable(usernameOrEmail)
            .filter(StringUtils::hasText)
            .flatMap(this::findUserInfo)
            .flatMap(this::buildUserLoginDTO);
    if (result.isPresent()) {
      UmsUserLoginDTO dto = result.get();
      log.debug("[getUserFromDB] success userId={}, accountName={}, roleId={}, costMs={}", dto.getUserId(), dto.getAccountName(), dto.getRoleId(), (System.currentTimeMillis()-t0));
    } else {
      log.warn("[getUserFromDB] NOT FOUND usernameOrEmail='{}' costMs={}", usernameOrEmail, (System.currentTimeMillis()-t0));
    }
    return result;
  }

  private Optional<UmsUserInfo> findUserInfo(String usernameOrEmail) {
    UmsUserInfo info = umsUserInfoMapper.selectOne(
            new LambdaQueryWrapper<UmsUserInfo>()
                    .eq(UmsUserInfo::getAccountName, usernameOrEmail)
    );
    if (info == null) {
      log.warn("[findUserInfo] accountName='{}' -> null", usernameOrEmail);
    } else {
      log.debug("[findUserInfo] found info.id={}, info.userId={}, accountName='{}'", info.getId(), info.getUserId(), info.getAccountName());
    }
    return Optional.ofNullable(info);
  }
  /**
   * 根據 UmsUserInfo並傳入User、UmsAuths、UmsCredentials 包裝建立 UmsUserLoginDTO
   */
  private Optional<UmsUserLoginDTO> buildUserLoginDTO(UmsUserInfo userInfo) {
    log.debug("[buildUserLoginDTO] input userInfo.id={}, userInfo.userId={}, accountName='{}'", userInfo.getId(), userInfo.getUserId(), userInfo.getAccountName());
    Optional<UmsUser> userOpt = findUserById(userInfo.getId());
    if (userOpt.isEmpty()) {
      log.warn("[buildUserLoginDTO] NO UmsUser via userInfo.id={} (可能 userInfo.userId={} 對應主鍵才是正確)" , userInfo.getId(), userInfo.getUserId());
    }
    return userOpt.flatMap(user -> buildCompleteUserDTO(user, userInfo));
  }
  /**
   * 根據 userInfoId 查找 UmsUser
   */
  private Optional<UmsUser> findUserById(Long userInfoId) {
    UmsUser user = umsUserMapper.selectOne(
            new LambdaQueryWrapper<UmsUser>()
                    .eq(UmsUser::getUserinfoId, userInfoId)
    );
    if (user == null) {
      log.warn("[findUserById] userinfoId={} -> null", userInfoId);
    } else {
      log.debug("[findUserById] found user.id={}, userinfoId={}, roleId={}, deleted={} ", user.getId(), user.getUserinfoId(), user.getRoleId(), user.getDeleted());
    }
    return Optional.ofNullable(user);
  }

  private Optional<UmsUserLoginDTO> buildCompleteUserDTO(UmsUser user, UmsUserInfo userInfo) {
    Long userId = user.getId();
    log.debug("[buildCompleteUserDTO] start user.id={}, pair userInfo.id={}, accountName='{}'", userId, userInfo.getId(), userInfo.getAccountName());

    Optional<UmsAuths> authsOpt = findAuthsByUserId(userId);
    Optional<UmsCredentials> credentialsOpt = findCredentialsByUserId(userId);

    if (authsOpt.isEmpty()) {
      log.warn("[buildCompleteUserDTO] missing UmsAuths userId={}", userId);
    }
    if (credentialsOpt.isEmpty()) {
      log.warn("[buildCompleteUserDTO] missing UmsCredentials userId={}", userId);
    }

    return authsOpt.flatMap(auths ->
            credentialsOpt.map(credentials -> {
              UmsUserLoginDTO dto = createUserLoginDTO(user, userInfo, auths, credentials);
              log.debug("[buildCompleteUserDTO] assembled DTO userId={}, accountName={}, emailPresent={}, pwdNull={} ", dto.getUserId(), dto.getAccountName(), dto.getEmail()!=null, dto.getPassword()==null);
              return dto;
            })
    );
  }

  private Optional<UmsAuths> findAuthsByUserId(Long userId) {
    UmsAuths auths = umsAuthsService.getOne(
            new LambdaQueryWrapper<UmsAuths>()
                    .eq(UmsAuths::getUserId, userId)
    );
    if (auths == null) {
      log.warn("[findAuthsByUserId] userId={} -> null", userId);
    } else {
      log.debug("[findAuthsByUserId] found auths.id={} userId={}", auths.getId(), auths.getUserId());
    }
    return Optional.ofNullable(auths);
  }

  private Optional<UmsCredentials> findCredentialsByUserId(Long userId) {
    UmsCredentials cred = umsCredentialsService.getOne(
            new LambdaQueryWrapper<UmsCredentials>()
                    .eq(UmsCredentials::getUserId, userId)
    );
    if (cred == null) {
      log.warn("[findCredentialsByUserId] userId={} -> null", userId);
    } else {
      log.debug("[findCredentialsByUserId] found credentials.id={} userId={} email='{}'", cred.getId(), cred.getUserId(), cred.getEmail());
    }
    return Optional.ofNullable(cred);
  }

  private UmsUserLoginDTO createUserLoginDTO(UmsUser user, UmsUserInfo userInfo,
                                             UmsAuths auths, UmsCredentials credentials) {
    UmsUserLoginDTO loginUser = new UmsUserLoginDTO();
    // 設定 DTO 的各個欄位
    loginUser.setUserId(user.getId());
    loginUser.setAccountName(userInfo.getAccountName());
    loginUser.setUserId(user.getId());
    loginUser.setDeleted(user.getDeleted());
    loginUser.setRoleId(user.getRoleId());
    loginUser.setPassword(auths.getPassword());
    loginUser.setEmail(credentials.getEmail());
    log.debug("[createUserLoginDTO] done userId={}, account='{}'", loginUser.getUserId(), loginUser.getAccountName());
    return loginUser;
  }




    public void createUserWithOpenId(UmsUser user, String openId, String platform) {
    System.out.println("在數據庫創建一個user"); // todo
    System.out.println("user綁定openId"); // todo
  }
}
