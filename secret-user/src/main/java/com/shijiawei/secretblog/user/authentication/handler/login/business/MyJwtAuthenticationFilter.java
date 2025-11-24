package com.shijiawei.secretblog.user.authentication.handler.login.business;

import java.io.IOException;
import java.util.Map;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
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
      // 未攜帶 JWT 視為未認證，統一回傳 401 以便前端攔截器觸發
//        throw new CustomRuntimeException("JWT token is missing!", HttpStatus.UNAUTHORIZED, "miss.token");
        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                .detailMessage("JWT token is missing!")
                .build();
    }

    try {
      UserLoginInfo userLoginInfo = jwtService.verifyJwt(jwtToken, UserLoginInfo.class);
      if (userLoginInfo == null) {
//        throw new CustomRuntimeException("jwt無效", HttpStatus.UNAUTHORIZED, "token.invalid");
          throw BusinessRuntimeException.builder()
                  .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                  .detailMessage("JWT token invalid")
                  .build();
      }
      // TEMP 黑名單校驗
      if (tokenBlacklistService != null && tokenBlacklistService.isBlacklisted(userLoginInfo.getSessionId())) {
//        logger.warn("Token sessionId={} 已在黑名單", userLoginInfo.getSessionId());
//        throw new CustomRuntimeException("jwt已失效", HttpStatus.UNAUTHORIZED, "token.blacklisted");
          throw BusinessRuntimeException.builder()
                  .iErrorCode(ResultCode.JWT_BLACKLISTED)
                  .detailMessage("JWT token in blacklisted")
                  .build();
      }
      MyJwtAuthentication authentication = new MyJwtAuthentication();
      authentication.setJwtToken(jwtToken);
      authentication.setAuthenticated(true); // 設置true，認證通過。
      authentication.setCurrentUser(userLoginInfo);
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }catch (ExpiredJwtException e) {
//      throw new CustomRuntimeException("jwt過期", HttpStatus.UNAUTHORIZED, "token.expired");
        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                .detailMessage("JWT token invalid")
                .build();
    } catch (Exception e) {
//      logger.error("JWT 校驗異常: {}", e.getMessage(), e);
//      throw new CustomRuntimeException("jwt無效", HttpStatus.UNAUTHORIZED, "token.invalid");
        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                .detailMessage("JWT token invalid")
                .build();
    }
    filterChain.doFilter(request, response);
  }
}
