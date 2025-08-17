package com.shijiawei.secretblog.user.authentication.handler.login.username;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.shijiawei.secretblog.common.utils.JSON;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 用戶名密碼登錄
 * AbstractAuthenticationProcessingFilter 的實現類要做的工作：
 * 1. 從HttpServletRequest提取授權憑證。假設用戶使用 用戶名/密碼 登錄，就需要在這裡提取username和password。
 *    然後，把提取到的授權憑證封裝到的Authentication對象，並且authentication.isAuthenticated()一定返回false
 * 2. 將Authentication對象傳給AuthenticationManager進行實際的授權操作
 */
public class UsernameAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

  private static final Logger logger = LoggerFactory.getLogger(UsernameAuthenticationFilter.class);

  public UsernameAuthenticationFilter(AntPathRequestMatcher pathRequestMatcher,
                                      AuthenticationManager authenticationManager,//url 相當於是Controller層中@PostMapping("/login/nickName")
                                      AuthenticationSuccessHandler authenticationSuccessHandler,
                                      AuthenticationFailureHandler authenticationFailureHandler) {
    super(pathRequestMatcher);
    setAuthenticationManager(authenticationManager);
    setAuthenticationSuccessHandler(authenticationSuccessHandler);
    setAuthenticationFailureHandler(authenticationFailureHandler);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
                                              HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
    logger.debug("use UsernameAuthenticationFilter");

    // 提取請求數據
    String requestJsonData = request.getReader().lines()
            .collect(Collectors.joining(System.lineSeparator()));
    
    // 檢查請求數據是否為空
    if (requestJsonData == null || requestJsonData.isEmpty()) {
        throw new AuthenticationException("請求數據不能為空") {};
    }
    
    Map<String, Object> requestMapData = JSON.parseToMap(requestJsonData);
    
    // 檢查解析後的 Map 是否為 null
    if (requestMapData == null) {
        throw new AuthenticationException("無法解析請求數據") {};
    }
    
    // 添加空值檢查
    if (requestMapData.get("accountName") == null) {
        throw new AuthenticationException("用戶名不能為空") {};
    }
    if (requestMapData.get("password") == null) {
        throw new AuthenticationException("密碼不能為空") {};
    }
    
    String accountName = requestMapData.get("accountName").toString();
    String password = requestMapData.get("password").toString();

    // 封裝成Spring Security需要的對象
    UsernameAuthentication authentication = new UsernameAuthentication();
    authentication.setUsername(accountName);
    authentication.setPassword(password);
    authentication.setAuthenticated(false);

    // 開始登錄認證。SpringSecurity會利用 Authentication對象去尋找 AuthenticationProvider進行登錄認證
    return getAuthenticationManager().authenticate(authentication);
  }

}
