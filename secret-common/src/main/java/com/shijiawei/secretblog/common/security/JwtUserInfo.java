package com.shijiawei.secretblog.common.security;

import com.shijiawei.secretblog.common.enumValue.Role;
import lombok.Data;

/**
 * JWT Payload（最小化使用者資訊）。
 *
 * */
@Data
public class JwtUserInfo {

    /**
     * 會話 ID（用於黑名單/登出失效）
     */
    private String sessionId;

    /**
     * 使用者 ID
     */
    private Long userId;

    /**
     * 暱稱或顯示名稱
     */
    private String nickname;

    /**
     * 角色
     */
    private Role roleId;

    /**
     * 頭像 URL（選用）
     */
    private String avatar;

    /**
     * Token 過期時間（毫秒，選用；實際過期以 JWT exp 為準）
     */
    private Long expiredTime;
}

