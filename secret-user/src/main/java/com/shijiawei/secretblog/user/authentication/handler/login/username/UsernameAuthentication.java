package com.shijiawei.secretblog.user.authentication.handler.login.username;



import com.shijiawei.secretblog.user.authentication.handler.login.UserLoginInfo;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;

/**
 * SpringSecurity傳輸登錄認證的數據的載體，相當一個Dto
 * 必須是 {@link Authentication} 實現類
 * 這裡選擇extends{@link AbstractAuthenticationToken}，而不是直接implements Authentication,
 * 是為了少些寫代碼。因為{@link Authentication}定義了很多接口，我們用不上。
 */
public class UsernameAuthentication extends AbstractAuthenticationToken {

  private String username; // 前端傳過來
  private String password; // 前端傳過來
  private UserLoginInfo currentUser; // 認證成功後，後台從數據庫獲取信息

  public UsernameAuthentication() {
    // 權限，用不上，直接null
    super(null);
  }

  @Override
  public Object getCredentials() {
    // 根據SpringSecurity的設計，授權成後，Credential（比如，登錄密碼）信息需要被清空
    return isAuthenticated() ? null : password;
  }

  @Override
  public Object getPrincipal() {
    // 根據SpringSecurity的設計，授權成功之前，getPrincipal返回的客戶端傳過來的數據。授權成功後，返回當前登陸用戶的信息
    return isAuthenticated() ? currentUser : username;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public UserLoginInfo getCurrentUser() {
    return currentUser;
  }

  public void setCurrentUser(UserLoginInfo currentUser) {
    this.currentUser = currentUser;
  }
}
