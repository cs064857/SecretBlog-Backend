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

    /**
     * 發送文章被回覆通知郵件
     *
     * @param to             收件人郵箱
     * @param articleTitle   文章標題（可為空）
     * @param replierNickname 回覆者暱稱（可為空）
     * @param articleId      文章ID（用於提供參考資訊）
     * @param replyContent   回覆內容摘要（可為空）
     * @param commentId      留言ID（用於提供參考資訊）
     */
    void sendArticleRepliedNotificationEmail(
            String to,
            String articleTitle,
            String replierNickname,
            Long articleId,
            String replyContent,
            Long commentId
    );

    /**
     * 發送留言被回覆通知郵件
     *
     * @param to              收件人郵箱
     * @param articleTitle    文章標題（可為空）
     * @param replierNickname 回覆者暱稱（可為空）
     * @param articleId       文章ID（用於提供參考資訊）
     * @param parentCommentId 父留言ID（用於提供參考資訊）
     * @param replyContent    回覆內容摘要（可為空）
     * @param commentId       新增留言ID（用於提供參考資訊）
     */
    void sendCommentRepliedNotificationEmail(
            String to,
            String articleTitle,
            String replierNickname,
            Long articleId,
            Long parentCommentId,
            String replyContent,
            Long commentId
    );
}
