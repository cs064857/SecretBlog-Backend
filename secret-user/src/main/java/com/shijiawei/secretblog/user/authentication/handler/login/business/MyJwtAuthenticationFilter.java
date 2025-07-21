package com.shijiawei.secretblog.user.authentication.handler.login.business;

import java.io.IOException;

import com.shijiawei.secretblog.common.utils.JwtService;
import jakarta.servlet.http.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.shijiawei.secretblog.common.exception.ExceptionTool;
import com.shijiawei.secretblog.user.authentication.handler.login.UserLoginInfo;


import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class MyJwtAuthenticationFilter extends OncePerRequestFilter {

  private static final Logger logger = LoggerFactory.getLogger(MyJwtAuthenticationFilter.class);

  private JwtService jwtService;

  public MyJwtAuthenticationFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    logger.debug("Use OpenApi1AuthenticationFilter");
    System.out.println("Use OpenApi1AuthenticationFilter");
//    String jwtToken = request.getHeader("Authorization");

    // 從 cookie 中獲取 token
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


    System.out.println("Authorization header: " + jwtToken);
    if (StringUtils.isEmpty(jwtToken)) {
      ExceptionTool.throwException("JWT token is missing!", "miss.token");
    }
//    if (jwtToken.startsWith("Bearer ")) {
//      jwtToken = jwtToken.substring(7);
//    }


    try {
      UserLoginInfo userLoginInfo = jwtService.verifyJwt(jwtToken, UserLoginInfo.class);

      MyJwtAuthentication authentication = new MyJwtAuthentication();
      authentication.setJwtToken(jwtToken);
      authentication.setAuthenticated(true); // 設置true，認證通過。
      authentication.setCurrentUser(userLoginInfo);
      // 認證通過後，一定要設置到SecurityContextHolder裡面去。
      SecurityContextHolder.getContext().setAuthentication(authentication);
    }catch (ExpiredJwtException e) {
      // 轉換異常，指定code，讓前端知道時token過期，去調刷新token接口
      ExceptionTool.throwException("jwt過期", HttpStatus.UNAUTHORIZED, "token.expired");
    } catch (Exception e) {
      ExceptionTool.throwException("jwt無效", HttpStatus.UNAUTHORIZED, "token.invalid");
    }
    // 放行
    filterChain.doFilter(request, response);
  }
}
