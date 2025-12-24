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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * 認證失敗時，會執行這個方法。將失敗原因告知客戶端
 */
@Component
public class CustomAuthenticationExceptionHandler implements
        AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        PrintWriter writer = response.getWriter();
        writer.print(JSON.stringify(R.error("認證失敗")));
        writer.flush();
        writer.close();
    }
}
