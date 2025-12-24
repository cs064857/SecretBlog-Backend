package com.shijiawei.secretblog.user.authentication.handler.exception;

import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

/**
 * 認證成功(Authentication), 但無權訪問時。會執行這個方法
 * 或者SpringSecurity框架捕捉到  AccessDeniedException時，會轉出
 */
@Component
public class CustomAuthorizationExceptionHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());
        PrintWriter writer = response.getWriter();
        writer.print(JSON.stringify(R.error("無權訪問")));
        writer.flush();
        writer.close();
    }
}
