package com.shijiawei.secretblog.user.service;

/**
 * EmailService
 * 負責發送各類系統郵件
 */
public interface EmailService {

    /**
     * 發送驗證碼郵件
     * @param to 收件人郵箱
     * @param code 驗證碼
     */
    void sendVerificationCodeEmail(String to, String code);

    /**
     * 發送密碼重設連結郵件
     * @param to 收件人郵箱
     * @param resetUrl 重設密碼的完整 URL
     */
    void sendPasswordResetEmail(String to, String resetUrl);
}
