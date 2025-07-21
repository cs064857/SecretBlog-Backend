package com.shijiawei.secretblog.user.authentication.handler.login;


import com.shijiawei.secretblog.common.exception.ExceptionTool;
import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.TimeTool;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import com.shijiawei.secretblog.common.utils.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

/**
 * 認證成功/登錄成功 事件處理器
 */
@Component
public class LoginSuccessHandler extends
        AbstractAuthenticationTargetUrlRequestHandler implements AuthenticationSuccessHandler {

  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  private JwtService jwtService;

  public LoginSuccessHandler() {
    this.setRedirectStrategy(new RedirectStrategy() {
      @Override
      public void sendRedirect(HttpServletRequest request, HttpServletResponse response, String url)
              throws IOException {
        // 更改重定向策略，前後端分離項目，後端使用RestFul風格，無需做重定向
        // Do nothing, no redirects in REST
      }
    });
  }

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {
    Object principal = authentication.getPrincipal();
    if (principal == null || !(principal instanceof UserLoginInfo)) {
      ExceptionTool.throwException(
              "登陸認證成功後，authentication.getPrincipal()返回的Object對象必須是：UserLoginInfo！");
    }
    UserLoginInfo currentUser = (UserLoginInfo) principal;
    currentUser.setSessionId(UUID.randomUUID().toString());

    // 生成token和refreshToken
    Map<String, Object> responseData = new LinkedHashMap<>();
    responseData.put("token", generateToken(currentUser));
    responseData.put("refreshToken", generateRefreshToken(currentUser));

    // 一些特殊的登錄參數。比如三方登錄，需要額外返回一個字段是否需要跳轉的綁定已有賬號頁面
    Object details = authentication.getDetails();
    if (details instanceof Map) {
      Map detailsMap = (Map)details;
      responseData.putAll(detailsMap);
    }

    // 雖然APPLICATION_JSON_UTF8_VALUE過時了，但也要用。因為Postman工具不聲明utf-8編碼就會出現亂碼
    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    PrintWriter writer = response.getWriter();
    writer.print(JSON.stringify(new R(200, "${login.success:登錄成功！}",responseData)));
    writer.flush();
    writer.close();
  }

  public String generateToken(UserLoginInfo currentUser) {
    long expiredTime = TimeTool.nowMilli() + TimeUnit.MINUTES.toMillis(10); // 10分鐘後過期
    currentUser.setExpiredTime(expiredTime);
    String jwt = jwtService.createJwt(currentUser, expiredTime);
    Map<String,Object> hashMap = jwtService.verifyJwt(jwt, HashMap.class);
    Set<Map.Entry<String, Object>> entrySet = hashMap.entrySet();
    entrySet.forEach(item-> {
      String key = item.getKey();
      if(key.equals("userId")){
        String userId = (String)item.getValue();
        System.out.println(userId);

      }
    });

    return jwt;
  }

  private String generateRefreshToken(UserLoginInfo loginInfo) {
    return jwtService.createJwt(loginInfo, TimeTool.nowMilli() + TimeUnit.DAYS.toMillis(30));
  }

}
