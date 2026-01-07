//package com.shijiawei.secretblog.user.authentication.controller;
//
//import com.shijiawei.secretblog.common.security.JwtUserInfo;
//import com.shijiawei.secretblog.common.utils.R;
//import com.shijiawei.secretblog.user.authentication.service.GoogleAuthService;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * ClassName: MeController
// * Description:
// *
// * @Create 2026/1/6 下午6:46
// */
//@RestController
//public class MeController {
//
//    @Lazy
//    @Autowired
//    private GoogleAuthService googleAuthService;
//
//    @GetMapping("/oauth2/success")
//    public R<Void> me(@AuthenticationPrincipal OAuth2User oauth2User, HttpServletRequest request , HttpServletResponse response) {
//        googleAuthService.getOauth2LoginSuccessInfo(oauth2User,response);
//        return R.ok();
//    }
//}
