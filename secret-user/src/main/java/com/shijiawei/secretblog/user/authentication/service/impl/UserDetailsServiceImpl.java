package com.shijiawei.secretblog.user.authentication.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.enumValue.Role;
import com.shijiawei.secretblog.common.exception.BusinessException;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.user.entity.*;
import com.shijiawei.secretblog.user.authentication.entity.LoginUser;
import com.shijiawei.secretblog.user.mapper.UmsUserInfoMapper;
import com.shijiawei.secretblog.user.mapper.UmsUserMapper;
import com.shijiawei.secretblog.user.service.UmsAuthsService;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ClassName: UserDetailsServiceImpl
 * Description: Spring Security 的 UserDetailsService 實作（帳號在 UmsUserInfo、密碼在 UmsAuths）
 *
 * @Create 2025/12/22 下午11:00
 */
@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UmsUserInfoMapper umsUserInfoMapper;

    @Autowired
    private UmsUserMapper umsUserMapper;

    @Autowired
    private UmsAuthsService umsAuthsService;

    @Autowired
    private UmsCredentialsService umsCredentialsService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!StringUtils.hasText(username)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("帳號不可為空")
                    .data(Map.of("accountName", Objects.requireNonNullElse(username, "[null]")))
                    .build();
        }

        // 1) 嘗試以帳號名稱 (accountName) 或 電子郵件 (email) 查找使用者
        UmsUser user = null;
        UmsUserInfo userInfo = null;

        // 先嘗試以 accountName 查找 UmsUserInfo
        userInfo = umsUserInfoMapper.selectOne(
                new LambdaQueryWrapper<UmsUserInfo>()
                        .eq(UmsUserInfo::getAccountName, username)
        );

        if (userInfo != null) {
            user = umsUserMapper.selectById(userInfo.getUserId());
        } else {
            // 若找不到，再嘗試以 email 查找 UmsCredentials
            UmsCredentials credentials = umsCredentialsService.getOne(
                    new LambdaQueryWrapper<UmsCredentials>()
                            .eq(UmsCredentials::getEmail, username)
            );
            if (credentials != null) {
                user = umsUserMapper.selectById(credentials.getUserId());
                if (user != null) {
                    // 取得對應的 userInfo 以便後續讀取暱稱/頭像（相容現有 DTO 邏輯）
                    userInfo = umsUserInfoMapper.selectOne(
                            new LambdaQueryWrapper<UmsUserInfo>()
                                    .eq(UmsUserInfo::getUserId, user.getId())
                    );
                }
            }
        }

        if (user == null || userInfo == null) {
            // 避免洩漏使用者是否存在，統一回傳認證失敗。
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("帳號或密碼錯誤")
                    .data(Map.of("accountName", username))
                    .build();
        }

        // 3) 取得 UmsAuths（密碼表）
        UmsAuths auths = umsAuthsService.getOne(
                new LambdaQueryWrapper<UmsAuths>()
                        .eq(UmsAuths::getUserId, user.getId())
        );
        if (auths == null || !StringUtils.hasText(auths.getPassword())) {
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.AUTH_INTERNAL_ERROR)
                    .detailMessage("找不到使用者密碼資訊，無法完成認證")
                    .data(Map.of(
                            "accountName", username,
                            "userId", Objects.requireNonNullElse(user.getId(), -1L)
                    ))
                    .build();
        }

        // 4) 組裝 DTO 並封裝成 UserDetails
        UmsUserLoginDTO dto = new UmsUserLoginDTO();
        dto.setUserId(user.getId());
        dto.setAccountName(userInfo.getAccountName());
        dto.setPassword(auths.getPassword());
        dto.setRoleId(user.getRoleId());
        dto.setDeleted(user.getDeleted());
        dto.setAvatar(user.getAvatar());

        //  改由 UmsCredentials 提供 email
        UmsCredentials credentials = umsCredentialsService.getOne(
                new LambdaQueryWrapper<UmsCredentials>()
                        .eq(UmsCredentials::getUserId, user.getId())
        );
        if (credentials != null && StringUtils.hasText(credentials.getEmail())) {
            dto.setEmail(credentials.getEmail());
        }

        // 5) 依用戶ID取得該用戶的角色列表，並轉換成 Spring Security authorities（ROLE_*）
        //    目前資料模型每個用戶僅有單一 role_id，但仍以 List 形式回傳以便後續擴充多角色。
        List<Role> roles = getRoleListByUserId(user.getId(), user);
        List<String> permissions = convertRolesToAuthorities(roles);

        log.debug("[loadUserByUsername] userId={}, accountName='{}', roleId={}, deleted={}, permissions={}",
                dto.getUserId(), dto.getAccountName(), dto.getRoleId(), dto.getDeleted(), permissions);

        return new LoginUser(dto, permissions);
    }

    private List<Role> getRoleListByUserId(Long userId, UmsUser user) {
        if (userId == null || user == null) {
            return List.of();
        }

        // 目前角色存放於 ums_user.role_id，這裡以 userId 為主語意取得角色列表。
        // 若未來擴充成多角色，可在此改為查詢 user-role 關聯表。
        Role roleId = user.getRoleId();
        if (roleId == null) {
            return List.of();
        }
        return List.of(roleId);
    }

    private List<String> convertRolesToAuthorities(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return new ArrayList<>();
        }

        LinkedHashSet<String> authorities = new LinkedHashSet<>();
        for (Role role : roles) {
            if (role == null) {
                continue;
            }
            switch (role) {
                case ADMIN -> {
                    authorities.add("ROLE_ADMIN");
                    authorities.add("ROLE_USER");
                }
                case NORMALUSER -> authorities.add("ROLE_USER");
            }
        }

        return new ArrayList<>(authorities);
    }
}
