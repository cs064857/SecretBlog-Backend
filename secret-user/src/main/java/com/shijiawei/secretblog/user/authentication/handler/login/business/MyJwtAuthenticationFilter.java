package com.shijiawei.secretblog.user.authentication.handler.login.business;

import java.io.IOException;

import com.shijiawei.secretblog.common.utils.JwtService;
import com.shijiawei.secretblog.user.authentication.handler.login.UserLoginInfo;
import com.shijiawei.secretblog.user.authentication.service.TokenBlacklistService; // TEMP 新增
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shijiawei.secretblog.common.exception.ExceptionTool;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MyJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(MyJwtAuthenticationFilter.class);

  private JwtService jwtService;
  private TokenBlacklistService tokenBlacklistService; // TEMP 新增黑名單服務

  // 原構造函數保留
  public MyJwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  // TEMP 新增構造以注入黑名單
  public MyJwtAuthenticationFilter(JwtService jwtService, TokenBlacklistService tokenBlacklistService) {
    this.jwtService = jwtService;
    this.tokenBlacklistService = tokenBlacklistService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    logger.debug("Use OpenApi1AuthenticationFilter");
    String jwtToken = null;
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if ("jwtToken".equals(cookie.getName())) {
          jwtToken = cookie.getValue();
          break;
        }
      }
    }
    logger.debug("JWT Token from cookie: {}", jwtToken);

    if (StringUtils.isEmpty(jwtToken)) {
      ExceptionTool.throwException("JWT token is missing!", "miss.token");
    }

    try {
      UserLoginInfo userLoginInfo = jwtService.verifyJwt(jwtToken, UserLoginInfo.class);
      if (userLoginInfo == null) {
        ExceptionTool.throwException("jwt無效", HttpStatus.UNAUTHORIZED, "token.invalid");
      }
      // TEMP 黑名單校驗
      if (tokenBlacklistService != null && tokenBlacklistService.isBlacklisted(userLoginInfo.getSessionId())) {
        logger.warn("Token sessionId={} 已在黑名單", userLoginInfo.getSessionId());
        ExceptionTool.throwException("jwt已失效", HttpStatus.UNAUTHORIZED, "token.blacklisted");
      }
      MyJwtAuthentication authentication = new MyJwtAuthentication();
      authentication.setJwtToken(jwtToken);
      authentication.setAuthenticated(true); // 設置true，認證通過。
      authentication.setCurrentUser(userLoginInfo);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }catch (ExpiredJwtException e) {
      ExceptionTool.throwException("jwt過期", HttpStatus.UNAUTHORIZED, "token.expired");
    } catch (Exception e) {
      logger.error("JWT 校驗異常: {}", e.getMessage(), e);
      ExceptionTool.throwException("jwt無效", HttpStatus.UNAUTHORIZED, "token.invalid");
    }
    filterChain.doFilter(request, response);
  }
}
