package com.shijiawei.secretblog.user.authentication.handler.exception.oauth2;

import com.shijiawei.secretblog.user.authentication.service.GoogleAuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * ClassName: OAuth2LoginSuccessHandler
 * Description:
 *
 * @Create 2026/1/6 下午7:44
 */
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${custom.front-domain}")
    private String frontDomain;

    private final GoogleAuthService googleAuthService;

    public OAuth2LoginSuccessHandler(@Lazy GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        // 寫入 JWT Cookie
        if (authentication != null && authentication.getPrincipal() instanceof OAuth2User oauth2User) {
            googleAuthService.getOauth2LoginSuccessInfo(oauth2User, response);
        }

        // 重定向到前端的路徑
       String frontendRedirectUrl = frontDomain + "/Home/2?page=1";
       response.sendRedirect(frontendRedirectUrl);
    }
}
