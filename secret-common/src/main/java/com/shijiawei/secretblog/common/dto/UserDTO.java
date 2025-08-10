package com.shijiawei.secretblog.common.dto;

import lombok.Data;

/**
 * 用戶信息傳輸對象
 * 用於微服務間數據傳輸，避免直接依賴其他服務的實體類
 */
@Data
public class UserDTO {
    /**
     * 用戶ID
     */
    private Long userId;

    /**
     * 用戶名
     */
    private String username;

    /**
     * 用戶頭像
     */
    private String avatar;

    /**
     * 賬號名稱（如果需要的話）
     */
    private String accountName;

    // 只包含當前服務需要的字段，不包含敏感信息如密碼等
}
