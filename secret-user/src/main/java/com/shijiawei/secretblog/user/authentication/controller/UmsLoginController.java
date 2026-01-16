package com.shijiawei.secretblog.user.authentication.controller;

import com.shijiawei.secretblog.common.security.JwtUserInfo;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.TimeTool;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.authentication.service.LoginService;
import com.shijiawei.secretblog.user.authentication.service.TokenBlacklistService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: UmsLoginController
 * Description:
 *
 * @Create 2025/12/23 上午12:12
 */
@RestController
@RequestMapping("/ums/user")
public class UmsLoginController {

    @Autowired
    private LoginService loginService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    /**
     * 登入入口。
     */
    @PostMapping({"/login/username"})
    public R login(@RequestBody UmsUserLoginDTO umsUserLoginDTO, HttpServletResponse response) {

        return loginService.login(umsUserLoginDTO, response);

    }
//    /**
//     * 用於測試權限 - 任一級別權限
//     * @return
//     */
//    @GetMapping("/test-any-verityToken")
//    @PreAuthorize("isAuthenticated()")//該端點權限要求為普通用戶等級
//    public R testAnyLogin() {
//        //測試是否成功登入
//        System.out.println("成功驗證Token並登入任一權限...");
//        return R.ok();
//    }
//
//    /**
//     * 用於測試權限 - 使用者級別權限
//     * @return
//     */
//    @GetMapping("/test-user-verityToken")
//    @PreAuthorize("hasAuthority('ROLE_USER')")//該端點權限要求為普通用戶等級
//    public R testLogin() {
//        //測試是否成功登入
//        System.out.println("成功驗證Token並登入使用者權限...");
//        return R.ok();
//    }

//    /**
//     * 用於測試權限 - 永遠不存在，只會失敗
//     *
//     * @return
//     */
//    @GetMapping("/test-unknow-verityToken")
//    @PreAuthorize("hasAuthority('ROLE_1155611')")//該端點權限要求永遠不會成功
//    public R testUnknowLogin() {
//        //測試是否成功登入
//        System.out.println("成功驗證Token並登入unknow權限...");
//        return R.ok();
//    }
//    /**
//     * 用於測試權限 - Admin權限
//     * @return
//     */
//    @GetMapping("/test-admin-verityToken")
//    @PreAuthorize("hasAuthority('ROLE_ADMIN')")//該端點權限要求為普通用戶等級
//    public R testAdminLogin() {
//        //測試是否成功登入
//        System.out.println("成功驗證Token並登入管理員權限...");
//        return R.ok();
//    }

    /**
     * 判斷是否已登入
     * @param authentication
     * @return
     */
    @GetMapping("/is-login")
    public R<String> isLogin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(String.valueOf(authentication.getPrincipal()))) {
            return new R<>("401", "未登入", null);
        }

        return R.ok("已登入", null);
    }

    /**
     * 登出入口。
     * JWT為無狀態，登出時將 sessionId 寫入黑名單(TTL=Token剩餘有效期)
     */
    @PreAuthorize("isAuthenticated()")//該端點權限要求為已登入
    @PostMapping("/logout")
    public R logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        // 以 Filter 建立的 principal（JwtUserInfo）為準，避免重複解析 JWT。
        if (authentication != null
                && authentication.getPrincipal() instanceof JwtUserInfo userInfo
                && StringUtils.hasText(userInfo.getSessionId())
                && userInfo.getExpiredTime() != null) {
            long ttlMillis = Math.max(userInfo.getExpiredTime() - TimeTool.nowMilli(), 1000L);
            tokenBlacklistService.blacklist(userInfo.getSessionId(), ttlMillis);
        }

        //清除前端可能使用的Cookie
        String[] cookiesToClear = {"jwtToken", "userId", "avatar"};
        for (String cookieName : cookiesToClear) {
            Cookie cookie = new Cookie(cookieName, null);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            response.addCookie(cookie);
        }

        SecurityContextHolder.clearContext();

        new SecurityContextLogoutHandler().logout(request, response, authentication);
        return R.ok("登出成功");
    }
}
