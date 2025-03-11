package com.shijiawei.secretblog.user.authentication.handler.login;

import java.io.IOException;
import java.io.PrintWriter;

import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;



import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * AbstractAuthenticationProcessingFilter拋出AuthenticationException異常後，會跑到這裡來
 */
@Component
public class LoginFailHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
    String errorMessage = exception.getMessage();
    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
    // 設置HTTP狀態碼為401 Unauthorized
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    PrintWriter writer = response.getWriter();
    R responseData = new R("login.fail",errorMessage);

    writer.print(JSON.stringify(responseData));
    writer.flush();
    writer.close();
  }
}
