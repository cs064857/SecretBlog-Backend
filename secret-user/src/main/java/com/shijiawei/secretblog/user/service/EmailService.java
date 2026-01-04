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

    /**
     * 發送文章被點讚通知郵件
     *
     * @param to               收件人郵箱
     * @param articleTitle     文章標題（可為空）
     * @param likedUserNickname 點讚者暱稱（可為空）
     * @param articleId        文章ID（用於提供參考資訊）
     */
    void sendArticleLikedNotificationEmail(String to, String articleTitle, String likedUserNickname, Long articleId);
}
