package com.shijiawei.secretblog.gateway.authentication.handler.login;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * 用戶信息登陸後的信息，會序列化到Jwt的payload
 */
public class UserLoginInfo {

  private String sessionId; // 會話id，全局唯一
  private Long userId; // 新增：用戶ID
  private String nickname; // 昵稱
  private String roleId;
  private Long expiredTime; // 過期時間

  // 添加 getAuthorities 方法以支持 Spring Security 認證
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Collection<GrantedAuthority> authorities = new ArrayList<>();
    if (roleId != null && !roleId.isEmpty()) {
      authorities.add(new SimpleGrantedAuthority("ROLE_" + roleId));
    }
    return authorities;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public String getNickname() {
    return nickname;
  }

  public void setNickname(String nickname) {
    this.nickname = nickname;
  }

  public Long getExpiredTime() {
    return expiredTime;
  }

  public void setExpiredTime(Long expiredTime) {
    this.expiredTime = expiredTime;
  }

  @Override
  public String toString() {
    return "UserLoginInfo{" +
            "sessionId='" + sessionId + '\'' +
            ", userId=" + userId +
            ", nickname='" + nickname + '\'' +
            ", roleId='" + roleId + '\'' +
            ", expiredTime=" + expiredTime +
            '}';
  }

  public static final class CurrentUserBuilder {

    private UserLoginInfo currentUser;

    private CurrentUserBuilder() {
      currentUser = new UserLoginInfo();
    }

    public static CurrentUserBuilder aCurrentUser() {
      return new CurrentUserBuilder();
    }

    public CurrentUserBuilder sessionId(String sessionId) {
      currentUser.setSessionId(sessionId);
      return this;
    }

    public CurrentUserBuilder userId(Long userId) {
      currentUser.setUserId(userId);
      return this;
    }

    public CurrentUserBuilder nickname(String nickname) {
      currentUser.setNickname(nickname);
      return this;
    }

    public CurrentUserBuilder roleId(String roleId) {
      currentUser.setRoleId(roleId);
      return this;
    }

    public CurrentUserBuilder expiredTime(Long expiredTime) {
      currentUser.setExpiredTime(expiredTime);
      return this;
    }

    public UserLoginInfo build() {
      return currentUser;
    }
  }
}
