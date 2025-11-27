package com.shijiawei.secretblog.user.authentication.handler.login;


import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.TimeTool;

import com.shijiawei.secretblog.user.authentication.service.JwtServiceActive;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.Cookie;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
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
  private JwtServiceActive jwtService;

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
//        throw new CustomRuntimeException(ResultCode.JWT_CONFIG_ERROR.getCode(), ResultCode.JWT_CONFIG_ERROR.getMessage(),"登陸認證成功後，authentication.getPrincipal()返回的Object對象必須是：UserLoginInfo！");
        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.AUTH_INTERNAL_ERROR)
                .detailMessage("登陸認證成功後，authentication.getPrincipal()返回的Object對象必須是：UserLoginInfo！")
                .build();
    }
    UserLoginInfo currentUser = (UserLoginInfo) principal;
    currentUser.setSessionId(UUID.randomUUID().toString());

    // 生成token和refreshToken
    Map<String, Object> responseData = new LinkedHashMap<>();
    String token = generateToken(currentUser); // TEMP 取得 token 以便下方設置 Cookie
    responseData.put("token", token);
    responseData.put("refreshToken", generateRefreshToken(currentUser));
    responseData.put("userId",currentUser.getUserId());



    // TEMP Set-Cookie: 將 JWT 寫入 HttpOnly Cookie (測試用途, 之後可改為 Secure; SameSite 設為 Lax 避免 csrf 基本攻擊)

      /**
       * 將jwtToken寫入Cookie中
       */
    final long jwtExpirationSeconds = 3600; // 假設 JWT 有效 1 小時
//    response.addHeader("Set-Cookie", "jwtToken=" + token + "; Path=/; Max-Age="+jwtExpirationSeconds+"; HttpOnly; SameSite=Lax");
//    response.addHeader("Set-Cookie", "userId=" + currentUser.getUserId() + "; Path=/; Max-Age="+jwtExpirationSeconds+"; HttpOnly; SameSite=Lax");

      ResponseCookie jwtCookie = ResponseCookie.from("jwtToken", token)
              .path("/")
              .maxAge(jwtExpirationSeconds)
              .httpOnly(true)//不允許前端JavaScript訪問Cookie
              .secure(true)
              .sameSite("Lax")
              .build();
      ResponseCookie userIdCookie = ResponseCookie.from("userId", String.valueOf(currentUser.getUserId()))
              .path("/")
              .maxAge(jwtExpirationSeconds)
              .httpOnly(false)
              .secure(true)
              .sameSite("Lax")
              .build();
      response.addHeader("Set-Cookie", jwtCookie.toString());
      response.addHeader("Set-Cookie", userIdCookie.toString());


    // 一些特殊的登錄參數。比如三方登錄，需要額外返回一個字段是否需要跳轉的綁定已有賬號頁面
    Object details = authentication.getDetails();
    if (details instanceof Map) {
      Map detailsMap = (Map)details;
      responseData.putAll(detailsMap);
    }

    // 雖然APPLICATION_JSON_UTF8_VALUE過時了，但也要用。因為Postman工具不聲明utf-8編碼就會出現亂碼
    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    PrintWriter writer = response.getWriter();
    writer.print(JSON.stringify(new R("200", "${login.success:登錄成功！}",responseData)));
    writer.flush();
    writer.close();
  }

  public String generateToken(UserLoginInfo currentUser) {
    //TODO 更改帳號過期時間
    // Token 過期時間(帳號過期、登入過期、jwt時間)，單位為分鐘
    long expiredTime = TimeTool.nowMilli() + TimeUnit.MINUTES.toMillis(300); // 300分鐘後過期
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
