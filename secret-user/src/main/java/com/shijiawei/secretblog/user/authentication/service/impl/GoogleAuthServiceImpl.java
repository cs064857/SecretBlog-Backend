package com.shijiawei.secretblog.user.authentication.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.enumValue.Role;
import com.shijiawei.secretblog.common.enumValue.Status;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.security.JwtService;
import com.shijiawei.secretblog.common.security.JwtUserInfo;
import com.shijiawei.secretblog.common.utils.TimeTool;
import com.shijiawei.secretblog.user.authentication.service.GoogleAuthService;
import com.shijiawei.secretblog.user.entity.UmsAuths;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.mapper.UmsUserInfoMapper;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
import com.shijiawei.secretblog.user.service.UmsAuthsService;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import com.shijiawei.secretblog.common.utils.AvatarUrlHelper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: GoogleAuthServiceImpl
 * Description:
 *
 * @Create 2026/1/6 下午8:08
 */
@Service
@Slf4j
public class GoogleAuthServiceImpl implements GoogleAuthService {

    @Value("${user.default-avatar:}")
    private String defaultAvatar;

    @Value("${custom.minio-domain}")
    private String minioDomain;

    @Value("${custom.domain:}")
    private String domain;

    private final UmsUserMapper umsUserMapper;

    private final UmsUserInfoMapper umsUserInfoMapper;

    private final UmsCredentialsService umsCredentialsService;

    private final UmsAuthsService umsAuthsService;

    private final JwtService jwtService;

    private final PasswordEncoder passwordEncoder;

    public GoogleAuthServiceImpl(
            UmsUserMapper umsUserMapper,
            UmsUserInfoMapper umsUserInfoMapper,
            UmsCredentialsService umsCredentialsService,
            UmsAuthsService umsAuthsService,
            JwtService jwtService,
            PasswordEncoder passwordEncoder
    ) {
        this.umsUserMapper = umsUserMapper;
        this.umsUserInfoMapper = umsUserInfoMapper;
        this.umsCredentialsService = umsCredentialsService;
        this.umsAuthsService = umsAuthsService;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Google OAuth2 登入成功後：
     * 1) 以 Email 判斷是否已註冊
     * 2) 未註冊則自動建立用戶資料（UmsUser/UmsUserInfo/UmsCredentials/UmsAuths）
     * 3) 依既有 username/password 登入流程生成 JWT，並寫入 Cookie 供前端後續請求使用
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void getOauth2LoginSuccessInfo(OAuth2User oauth2User, HttpServletResponse response) {
        if (oauth2User == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("OAuth2User 不可為空")
                    .build();
        }
        if (response == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("HttpServletResponse 不可為空")
                    .build();
        }

        String email = Objects.toString(oauth2User.getAttribute("email"), "");
        String googleId = Objects.toString(oauth2User.getName(), "");
        String name = Objects.toString(oauth2User.getAttribute("name"), "");
        String pictureUrl = Objects.toString(oauth2User.getAttribute("picture"), "");

        if (!org.springframework.util.StringUtils.hasText(email)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("Google 帳號未提供 email，無法完成登入")
                    .data(Map.of(
                            "googleId", StringUtils.defaultString(googleId, ""),
                            "name", StringUtils.defaultString(name, "")
                    ))
                    .build();
        }

        UmsCredentials credentials = umsCredentialsService.getOne(
                new LambdaQueryWrapper<UmsCredentials>()
                        .eq(UmsCredentials::getEmail, email)
                        .last("limit 1")
        );

        UmsUser user;
        UmsUserInfo userInfo;

        if (credentials == null) {
            // 尚未註冊：自動註冊
            UserBundle bundle = autoRegisterByGoogle(email, name, pictureUrl);
            user = bundle.user;
            userInfo = bundle.userInfo;
            log.info("Google OAuth2 自動註冊完成，userId={}, email={}", user.getId(), email);
        } else {
            user = umsUserMapper.selectById(credentials.getUserId());
            if (user == null) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.AUTH_INTERNAL_ERROR)
                        .detailMessage("找到憑證但找不到使用者資料，無法完成 OAuth2 登入")
                        .data(Map.of(
                                "email", email,
                                "userId", Objects.requireNonNullElse(credentials.getUserId(), -1L)
                        ))
                        .build();
            }
            userInfo = umsUserInfoMapper.selectOne(
                    new LambdaQueryWrapper<UmsUserInfo>()
                            .eq(UmsUserInfo::getUserId, user.getId())
                            .last("limit 1")
            );
            if (userInfo == null) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.AUTH_INTERNAL_ERROR)
                        .detailMessage("找到使用者但找不到使用者資訊，無法完成 OAuth2 登入")
                        .data(Map.of(
                                "email", email,
                                "userId", Objects.requireNonNullElse(user.getId(), -1L)
                        ))
                        .build();
            }
        }

        writeJwtCookie(response, user, userInfo);
    }

    private void writeJwtCookie(HttpServletResponse response, UmsUser user, UmsUserInfo userInfo) {
        long expiredAt = TimeTool.nowMilli() + TimeUnit.DAYS.toMillis(30);

        String avatarUrl = AvatarUrlHelper.toPublicUrl(user.getAvatar(), minioDomain);

        JwtUserInfo jwtUserInfo = new JwtUserInfo();
        jwtUserInfo.setSessionId(UUID.randomUUID().toString());
        jwtUserInfo.setUserId(user.getId());
        jwtUserInfo.setNickname(StringUtils.defaultIfBlank(user.getNickName(), userInfo.getAccountName()));
        jwtUserInfo.setAvatar(avatarUrl);
        jwtUserInfo.setExpiredTime(expiredAt);
        jwtUserInfo.setRoleId(user.getRoleId());

        String token = jwtService.createJwt(jwtUserInfo, expiredAt);

        int maxAgeSeconds = (int) TimeUnit.DAYS.toSeconds(30);

        ResponseCookie.ResponseCookieBuilder jwtCookieBuilder = ResponseCookie.from("jwtToken", token)
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax");

        ResponseCookie.ResponseCookieBuilder userIdCookieBuilder = ResponseCookie.from("userId", String.valueOf(user.getId()))
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(false)
                .secure(true)
                .sameSite("Lax");


        ResponseCookie.ResponseCookieBuilder avatarCookieBuilder = ResponseCookie.from("avatar", StringUtils.defaultString(avatarUrl, ""))
                .path("/")
                .maxAge(maxAgeSeconds)
                .httpOnly(false)
                .secure(true)
                .sameSite("Lax");

        if(org.springframework.util.StringUtils.hasText(domain)){
            avatarCookieBuilder.domain(domain);
            userIdCookieBuilder.domain(domain);
            jwtCookieBuilder.domain(domain);
        }


        response.addHeader("Set-Cookie", jwtCookieBuilder.build().toString());
        response.addHeader("Set-Cookie", userIdCookieBuilder.build().toString());
        response.addHeader("Set-Cookie", avatarCookieBuilder.build().toString());
    }

    private UserBundle autoRegisterByGoogle(String email, String name, String pictureUrl) {
        LocalDateTime now = LocalDateTime.now();

        String accountName = generateUniqueAccountName(email);
        String nickName = StringUtils.defaultIfBlank(name, accountName);
        String avatar = StringUtils.defaultIfBlank(pictureUrl, defaultAvatar);
        String storedAvatar = AvatarUrlHelper.toStoragePath(avatar, minioDomain);

        UmsUser user = UmsUser.builder()
                .nickName(nickName)
                .roleId(Role.NORMALUSER)
                .avatar(storedAvatar)
                .status(Status.NORMAL)
                .deleted((byte) 0)
                .createAt(now)
                .updateAt(now)
                .build();
        long userId = IdWorker.getId(user);
        user.setId(userId);

        UmsUserInfo userInfo = UmsUserInfo.builder()
                .userId(userId)
                .accountName(accountName)
                .notifyEnabled((byte) 1)
                .createAt(now)
                .updateAt(now)
                .build();
        long userInfoId = IdWorker.getId(userInfo);
        userInfo.setId(userInfoId);
        
        // 關聯 userInfoId 到 user
        user.setUserinfoId(userInfoId);

        UmsCredentials credentials = UmsCredentials.builder()
                .id(IdWorker.getId(UmsCredentials.class))
                .userId(userId)
                .email(email)
                .createAt(now)
                .updateAt(now)
                .build();

        // 產生一組不可預測的初始密碼，避免空密碼造成後續登入/改密碼流程異常。
        UmsAuths auths = UmsAuths.builder()
                .id(IdWorker.getId(UmsAuths.class))
                .userId(userId)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .passwordUpdatedAt(now)
                .createAt(now)
                .updateAt(now)
                .build();

        umsUserMapper.insert(user);
        umsUserInfoMapper.insert(userInfo);
        umsCredentialsService.save(credentials);
        umsAuthsService.save(auths);

        return new UserBundle(user, userInfo);
    }

    private String generateUniqueAccountName(String email) {
        String emailPrefix = StringUtils.substringBefore(email, "@");
        if (!org.springframework.util.StringUtils.hasText(emailPrefix)) {
            emailPrefix = "user";
        }

        String candidate = emailPrefix;
        int suffix = 0;
        while (accountNameExists(candidate)) {
            suffix++;
            candidate = emailPrefix + "_" + suffix;
            if (suffix >= 50) {
                candidate = emailPrefix + "_" + UUID.randomUUID().toString().substring(0, 8);
                break;
            }
        }
        return candidate;
    }

    private boolean accountNameExists(String accountName) {
        if (!org.springframework.util.StringUtils.hasText(accountName)) {
            return false;
        }

        return umsUserInfoMapper.selectCount(
                new LambdaQueryWrapper<UmsUserInfo>()
                        .eq(UmsUserInfo::getAccountName, accountName)
        ) > 0;
    }

    private static class UserBundle {
        private final UmsUser user;
        private final UmsUserInfo userInfo;

        private UserBundle(UmsUser user, UmsUserInfo userInfo) {
            this.user = user;
            this.userInfo = userInfo;
        }
    }
}
