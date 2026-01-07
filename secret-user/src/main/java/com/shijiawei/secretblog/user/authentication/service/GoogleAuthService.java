package com.shijiawei.secretblog.user.authentication.service;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * ClassName: GoogleAuthService
 * Description:
 *
 * @Create 2026/1/6 下午8:06
 */
public interface GoogleAuthService {

    void getOauth2LoginSuccessInfo(OAuth2User oauth2User, HttpServletResponse response);

}
