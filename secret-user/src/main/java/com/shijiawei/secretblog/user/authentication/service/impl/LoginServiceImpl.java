package com.shijiawei.secretblog.user.authentication.service.impl;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessException;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.TimeTool;
import com.shijiawei.secretblog.common.security.JwtService;
import com.shijiawei.secretblog.common.security.JwtUserInfo;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.authentication.entity.LoginUser;
import com.shijiawei.secretblog.user.authentication.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: LoginServiceImpl
 * Description:
 *
 * @Create 2025/12/23 上午12:14
 */
@Service
public class LoginServiceImpl implements LoginService {

    //注入在SecurityConfig中加載的AuthenticationManager
    @Autowired
    private AuthenticationManager authenticationManager;
    //注入JwtService，用於生成jwtToken
    @Autowired
    private JwtService jwtService;

    public R login(UmsUserLoginDTO umsUserLoginDTO){
        if (umsUserLoginDTO == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("登入資訊不可為空")
                    .build();
        }
        if (!StringUtils.hasText(umsUserLoginDTO.getAccountName()) || !StringUtils.hasText(umsUserLoginDTO.getPassword())) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("帳號或密碼不可為空")
                    .data(Map.of(
                            "accountName", Objects.requireNonNullElse(umsUserLoginDTO.getAccountName(), "[null]")
                    ))
                    .build();
        }

        /**
         * 驗證帳號密碼是否正確並返回認證結果
         */

        //AuthenticationManager需要Authentication實現類，傳入帳號以及密碼
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(umsUserLoginDTO.getAccountName(),umsUserLoginDTO.getPassword());

        //調用AuthenticationManager的authenticate()實施驗證，會去調用UserDetailsServiceImpl中的loadUserByUsername函數
        Authentication authenticate;
        try {
            authenticate = authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            RuntimeException businessException = unwrapBusinessException(e);
            if (businessException != null) {
                throw businessException;
            }
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("登入失敗：帳號或密碼錯誤")
                    .cause(e)
                    .data(Map.of(
                            "accountName", Objects.requireNonNullElse(umsUserLoginDTO.getAccountName(), "[null]")
                    ))
                    .build();
        } catch (Exception e) {
            RuntimeException businessException = unwrapBusinessException(e);
            if (businessException != null) {
                throw businessException;
            }
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.AUTH_INTERNAL_ERROR)
                    .detailMessage("登入流程發生非預期錯誤")
                    .cause(e)
                    .data(Map.of(
                            "accountName", Objects.requireNonNullElse(umsUserLoginDTO.getAccountName(), "[null]")
                    ))
                    .build();
        }

        //如果認證成功，則會返回NewLoginUser(UserDetails實現類)的結果資料
        LoginUser currentUser = (LoginUser)authenticate.getPrincipal();

        UmsUserLoginDTO userLoginDTO = currentUser.getUmsUserLoginDTO();
        Long userId = userLoginDTO.getUserId();

        /**
         * 生成jwtToken並返回
          */

        //包裝token裡存放的資訊，創建JwtUserInfo物件，並設定會話ID、使用者ID、暱稱、頭像URL、過期時間、角色ID
        Map<String, Object> responseData = new LinkedHashMap<>();
        long expiredAt = TimeTool.nowMilli() + TimeUnit.DAYS.toMillis(30);
        JwtUserInfo jwtUserInfo = new JwtUserInfo();
        jwtUserInfo.setSessionId(UUID.randomUUID().toString());
        jwtUserInfo.setUserId(userId);
        jwtUserInfo.setNickname(userLoginDTO.getAccountName());
        jwtUserInfo.setAvatar(userLoginDTO.getAvatar());
        jwtUserInfo.setExpiredTime(expiredAt);
        jwtUserInfo.setRoleId(userLoginDTO.getRoleId());

        //創建jwtToken
        String token = jwtService.createJwt(jwtUserInfo, expiredAt);

        //返回值中存入token以及userId
        responseData.put("token", token);
        responseData.put("userId",userId);

        //返回jwtToken以及userId
        return R.ok(responseData);
    }

    private static RuntimeException unwrapBusinessException(Throwable throwable) {
        Throwable cursor = throwable;
        while (cursor != null) {
            if (cursor instanceof BusinessRuntimeException businessRuntimeException) {
                return businessRuntimeException;
            }
            if (cursor instanceof BusinessException businessException) {
                return businessException;
            }
            cursor = cursor.getCause();
        }
        return null;
    }

}
