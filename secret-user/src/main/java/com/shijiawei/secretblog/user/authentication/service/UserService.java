package com.shijiawei.secretblog.user.authentication.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.mapper.UmsUserInfoMapper;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;

import java.util.Optional;

@Service
public class UserService {

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UmsUserMapper umsUserMapper;

  @Autowired
  private UmsUserInfoMapper umsUserInfoMapper;

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
//      testUser.setUsername("Tom");
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
//      testUser.setUsername("manager");
//      testUser.setRoleId("manager");
//      testUser.setPassword(passwordEncoder.encode("manager"));
//      testUser.setPhone("1234567890");
      return testUser;
    }
    return null;
  }

//  public UmsUserLoginDTO getUserFromDB(String username) {
//
//    UmsUserInfo umsUserInfo = umsUserInfoMapper.selectOne(new LambdaQueryWrapper<UmsUserInfo>().eq(UmsUserInfo::getAccountName, username));
//
//    if(umsUserInfo!=null){
//      UmsUser umsUser = umsUserMapper.selectOne(new LambdaQueryWrapper<UmsUser>().eq(UmsUser::getUserinfoId, umsUserInfo.getId()));
//      if (username.equals(umsUserInfo.getAccountName())) {
////      if (username.equals("admin")) {
//        UmsUserLoginDTO testUser = new UmsUserLoginDTO();
//        testUser.setAccountName(umsUserInfo.getAccountName());
//        testUser.setDeleted(umsUser.getDeleted());
//        testUser.setEmail(umsUserInfo.getEmail());
//        testUser.setRoleId(umsUser.getRoleId());
//        testUser.setPassword(passwordEncoder.encode(umsUserInfo.getPassword()));
////      testUser.setUserId(1001L);
////      testUser.setUsername("admin");
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

  public UmsUserLoginDTO getUserFromDB(String usernameOrEmail) {
    // 使用 lambda 表達式結合 Optional 處理查詢邏輯
    return Optional.ofNullable(
                    // 先嘗試以 accountName 查詢
                    umsUserInfoMapper.selectOne(
                            new LambdaQueryWrapper<UmsUserInfo>()
                                    .eq(UmsUserInfo::getAccountName, usernameOrEmail)
                    )
            )
            .or(() -> Optional.ofNullable(
                    // 如果以 accountName 找不到，再以 email 查詢
                    umsUserInfoMapper.selectOne(
                            new LambdaQueryWrapper<UmsUserInfo>()
                                    .eq(UmsUserInfo::getEmail, usernameOrEmail)
                    )
            ))
            .map(umsUserInfo -> {
              // 查詢對應的 UmsUser
              UmsUser umsUser = umsUserMapper.selectOne(
                      new LambdaQueryWrapper<UmsUser>()
                              .eq(UmsUser::getUserinfoId, umsUserInfo.getId())
              );

              // 構建並返回 UmsUserLoginDTO
              return Optional.ofNullable(umsUser)
                      .map(user -> {
                        UmsUserLoginDTO loginUser = new UmsUserLoginDTO();
                        loginUser.setUserId(umsUserInfo.getUserId());
                        loginUser.setAccountName(umsUserInfo.getAccountName());
                        loginUser.setDeleted(user.getDeleted());
                        loginUser.setEmail(umsUserInfo.getEmail());
                        loginUser.setRoleId(user.getRoleId());
                        loginUser.setPassword(passwordEncoder.encode(umsUserInfo.getPassword()));
                        return loginUser;
                      })
                      .orElse(null);
            })
            .orElse(null);
  }

  public void createUserWithOpenId(UmsUser user, String openId, String platform) {
    System.out.println("在數據庫創建一個user"); // todo
    System.out.println("user綁定openId"); // todo
  }
}
