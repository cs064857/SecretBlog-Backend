package com.shijiawei.secretblog.user.service.Impl;

import com.shijiawei.secretblog.user.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * EmailServiceImpl
 * 使用 JavaMailSender 發送郵件
 */
@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${custom.project-name:SecretBlog}")
    private String applicationName;

    @Override
    public void sendVerificationCodeEmail(String to, String code) {
        String subject = "【" + applicationName + "】電子郵件驗證碼";
        String content = buildVerificationEmailContent(code, "註冊帳號");
        sendHtmlEmail(to, subject, content);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetUrl) {
        String subject = "【" + applicationName + "】密碼重設請求";
        String content = buildPasswordResetEmailContent(resetUrl);
        sendHtmlEmail(to, subject, content);
    }

    @Override
    public void sendArticleLikedNotificationEmail(String to, String articleTitle, String likedUserNickname, Long articleId) {
        String subject = "【" + applicationName + "】你的文章被點讚了";
        String content = buildArticleLikedNotificationEmailContent(articleTitle, likedUserNickname, articleId);
        sendHtmlEmail(to, subject, content);
    }

    /**
     * 發送 HTML 格式郵件
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("郵件發送成功，收件人: {}", to);
        } catch (MessagingException e) {
            log.error("郵件發送失敗，收件人: {}, 錯誤: {}", to, e.getMessage());
            throw new RuntimeException("郵件發送失敗", e);
        }
    }

    /**
     * 構建驗證碼郵件 HTML 內容
     */
    private String buildVerificationEmailContent(String code, String purpose) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Microsoft JhengHei', Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #333; margin-bottom: 30px; }
                    .code-box { background-color: #f8f9fa; border: 2px dashed #007bff; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }
                    .code { font-size: 32px; font-weight: bold; color: #007bff; letter-spacing: 8px; }
                    .info { color: #666; font-size: 14px; line-height: 1.6; }
                    .warning { color: #dc3545; font-size: 12px; margin-top: 20px; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2 class="header">%s 驗證碼</h2>
                    <p class="info">您好，</p>
                    <p class="info">您正在進行 <strong>%s</strong> 操作，請使用以下驗證碼：</p>
                    <div class="code-box">
                        <span class="code">%s</span>
                    </div>
                    <p class="info">此驗證碼 <strong>15 分鐘</strong>內有效，請儘快完成驗證。</p>
                    <p class="warning">⚠️ 如果這不是您本人的操作，請忽略此郵件。</p>
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>© %s %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(applicationName, purpose, code, java.time.Year.now().getValue(), applicationName);
    }

    /**
     * 構建密碼重設連結郵件 HTML 內容
     */
    private String buildPasswordResetEmailContent(String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Microsoft JhengHei', Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #333; margin-bottom: 30px; }
                    .btn-box { text-align: center; margin: 30px 0; }
                    .btn { display: inline-block; background-color: #007bff; color: #ffffff !important; text-decoration: none; padding: 14px 32px; border-radius: 6px; font-size: 16px; font-weight: bold; }
                    .btn:hover { background-color: #0056b3; }
                    .info { color: #666; font-size: 14px; line-height: 1.6; }
                    .url-box { background-color: #f8f9fa; border-radius: 6px; padding: 12px; margin: 20px 0; word-break: break-all; font-size: 12px; color: #666; }
                    .warning { color: #dc3545; font-size: 12px; margin-top: 20px; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2 class="header">密碼重設請求</h2>
                    <p class="info">您好，</p>
                    <p class="info">我們收到了您的密碼重設請求。請點擊下方按鈕重設您的密碼：</p>
                    <div class="btn-box">
                        <a href="%s" class="btn">重設密碼</a>
                    </div>
                    <p class="info">如果按鈕無法點擊，請複製以下連結到瀏覽器：</p>
                    <div class="url-box">%s</div>
                    <p class="info">此連結 <strong>30 分鐘</strong>內有效，且僅能使用一次。</p>
                    <p class="warning">⚠️ 如果這不是您本人的操作，請忽略此郵件，您的密碼將保持不變。</p>
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>© %s %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(resetUrl, resetUrl, java.time.Year.now().getValue(), applicationName);
    }

    /**
     * 構建文章被點讚通知郵件 HTML 內容
     */
    private String buildArticleLikedNotificationEmailContent(String articleTitle, String likedUserNickname, Long articleId) {
        String safeTitle = StringUtils.defaultIfBlank(articleTitle, "（未命名文章）");
        String safeNickname = StringUtils.defaultIfBlank(likedUserNickname, "某位使用者");
        String safeArticleId = articleId == null ? "（未知）" : String.valueOf(articleId);

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Microsoft JhengHei', Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background-color: #ffffff; border-radius: 8px; padding: 30px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .header { text-align: center; color: #333; margin-bottom: 30px; }
                    .info { color: #666; font-size: 14px; line-height: 1.6; }
                    .highlight { color: #007bff; font-weight: bold; }
                    .footer { text-align: center; color: #999; font-size: 12px; margin-top: 30px; border-top: 1px solid #eee; padding-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2 class="header">文章被點讚通知</h2>
                    <p class="info">您好，</p>
                    <p class="info">你的文章 <span class="highlight">%s</span> 已被 <span class="highlight">%s</span> 點讚。</p>
                    <p class="info">文章ID：%s</p>
                    <div class="footer">
                        <p>此郵件由系統自動發送，請勿直接回覆。</p>
                        <p>© %s %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                escapeHtml(safeTitle),
                escapeHtml(safeNickname),
                escapeHtml(safeArticleId),
                java.time.Year.now().getValue(),
                applicationName
            );
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
