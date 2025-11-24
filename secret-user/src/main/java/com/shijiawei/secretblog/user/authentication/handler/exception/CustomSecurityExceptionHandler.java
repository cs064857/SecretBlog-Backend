package com.shijiawei.secretblog.user.authentication.handler.exception;

import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.JSON;
import com.shijiawei.secretblog.common.utils.R;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 捕捉Spring security filter chain 中拋出的未知異常
 */
public class CustomSecurityExceptionHandler extends OncePerRequestFilter {

    public static final Logger logger = LoggerFactory.getLogger(
            CustomSecurityExceptionHandler.class);

    @Override
    public void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                 FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
//        }
//        catch (BusinessRuntimeException e) {
//            // 自定義異常
//            R result = new R(e.getCode(),e.getMessage());
////            Result result = ResultBuilder.aResult()
////                    .msg(e.getMessage())
////                    .code(e.getCode())
////                    .build();
//            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
//            response.setStatus(e.getHttpStatus().value());
//            PrintWriter writer = response.getWriter();
//            writer.write(JSON.stringify(result));
//            writer.flush();
//            writer.close();
        } catch (AuthenticationException | AccessDeniedException e) {
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpStatus.FORBIDDEN.value());
            PrintWriter writer = response.getWriter();
            writer.print(JSON.stringify(R.error(e.getMessage())));
            writer.flush();
            writer.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            // 未知異常
            R result = new R("System Error","system.error");
            response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            PrintWriter writer = response.getWriter();
            writer.write(JSON.stringify(result));
            writer.flush();
            writer.close();
        }
    }
}
