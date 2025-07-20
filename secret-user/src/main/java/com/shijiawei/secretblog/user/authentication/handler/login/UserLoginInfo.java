package com.shijiawei.secretblog.user.authentication.handler.login;

/**
 * 用戶信息登陸後的信息，會序列化到Jwt的payload
 */
public class UserLoginInfo {

  private String sessionId; // 會話id，全局唯一
  private Long userId;
  private String nickname; // 昵稱
  private String roleId;

  private Long expiredTime; // 過期時間

  public Long getUserId(){
    return userId;
  }

  public void setUserId(Long userId){
    this.userId=userId;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

//  public Long getUserId() {
//    return userId;
//  }
//
//  public void setUserId(Long userId) {
//    this.userId = userId;
//  }

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
//            ", userId=" + userId +
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

//    public CurrentUserBuilder userId(Long userId) {
//      currentUser.setUserId(userId);
//      return this;
//    }

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
