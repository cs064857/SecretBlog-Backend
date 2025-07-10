package com.shijiawei.secretblog.user.authentication.handler.login.username;



import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.user.authentication.handler.login.UserLoginInfo;
import com.shijiawei.secretblog.user.authentication.service.UserService;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.mapper.UmsUserInfoMapper;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;

/**
 * 帳號密碼登錄認證
 */
@Component
@Slf4j
public class UsernameAuthenticationProvider implements AuthenticationProvider {

  @Autowired
  private UserService userService;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private UmsUserInfoMapper userInfoMapper;

  @Autowired
  private UmsUserMapper userMapper;

  public UsernameAuthenticationProvider() {
    super();
  }

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {
    // 用戶提交的用戶名 + 密碼：
    String usernameOrEmail = (String)authentication.getPrincipal();
    String password = (String) authentication.getCredentials();
    System.out.println("校驗：UserName/Email:"+usernameOrEmail+",Password:"+password);

    ///TODO 實現登入邏輯
    //相當於是Controller層中//2、校驗用戶名及密碼邏輯，自行實現登入邏輯
    /*
      查數據庫，匹配用戶信息
     */

    UmsUserLoginDTO UmsUserLoginDTO = userService.getUserFromDB(usernameOrEmail);
    log.info("UmsUserLoginDTO:{}",UmsUserLoginDTO);
    if (UmsUserLoginDTO == null
//            || Byte.valueOf((byte)1).equals(userLoginDTO.getPassword())
            || !passwordEncoder.matches(password, UmsUserLoginDTO.getPassword())) {
      // 密碼錯誤，直接拋異常。
      // 根據SpringSecurity框架的代碼邏輯，認證失敗時，應該拋這個異常：org.springframework.security.core.AuthenticationException
      // BadCredentialsException就是這個異常的子類
      // 拋出異常後後，AuthenticationFailureHandler的實現類會處理這個異常。
      throw new BadCredentialsException("${invalid.username.or.pwd:用戶名或密碼不正確}");
    }

    //創建Token
    UsernameAuthentication token = new UsernameAuthentication();
    //設置用戶
    UserLoginInfo userLoginInfo = new UserLoginInfo();
//    userLoginInfo.setSessionId();
//    userLoginInfo.setExpiredTime();
    userLoginInfo.setNickname(UmsUserLoginDTO.getAccountName());
    userLoginInfo.setRoleId(UmsUserLoginDTO.getRoleId().toString());
    token.setCurrentUser(userLoginInfo);
    token.setAuthenticated(true); // 認證通過，這裡一定要設成true
    return token;
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.isAssignableFrom(UsernameAuthentication.class);
  }
}

