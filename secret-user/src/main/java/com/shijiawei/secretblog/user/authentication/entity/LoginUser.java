package com.shijiawei.secretblog.user.authentication.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.shijiawei.secretblog.user.DTO.UmsUserLoginDTO;
import com.shijiawei.secretblog.common.enumValue.Status;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ClassName: LoginUser
 * Description: Spring Security 的 UserDetails 實作，封裝登入所需的使用者資訊
 *
 * @Create 2025/12/22 下午11:01
 */
@Data
@NoArgsConstructor
public class LoginUser implements UserDetails {

    private UmsUserLoginDTO umsUserLoginDTO;

    //添加所有可用的權限
    @JsonInclude//不序列化權限列表
    private List<String> permissions;

    public LoginUser(UmsUserLoginDTO umsUserLoginDTO, List<String> permissions) {
        this.umsUserLoginDTO = umsUserLoginDTO;
        this.permissions = permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 將權限字串列表轉換成 GrantedAuthority 集合
        if (permissions == null || permissions.isEmpty()) {
            return new ArrayList<>();
        }
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {

        return umsUserLoginDTO.getPassword();
    }

    @Override
    public String getUsername() {

        return umsUserLoginDTO.getAccountName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return umsUserLoginDTO.getStatus() == Status.NORMAL;
    }
}
