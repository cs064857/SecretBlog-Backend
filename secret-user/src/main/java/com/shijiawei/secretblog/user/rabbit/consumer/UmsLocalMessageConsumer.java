package com.shijiawei.secretblog.user.rabbit.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import com.shijiawei.secretblog.common.message.ArticleRepliedEmailNotifyMessage;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.common.message.CommentRepliedEmailNotifyMessage;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.entity.UmsUserInfo;
import com.shijiawei.secretblog.user.feign.ArticleFeignClient;
import com.shijiawei.secretblog.user.service.EmailService;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * ClassName: UmsLocalMessageConsumer
 * Description:
 *
 * @Create 2025/12/1 下午6:37
 */
@Slf4j
@Component
public class UmsLocalMessageConsumer {
    @Autowired
    private ArticleFeignClient articleFeignClient;

    @Autowired
    private UmsCredentialsService umsCredentialsService;

    @Autowired
    private UmsUserInfoService umsUserInfoService;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private EmailService emailService;

    @RabbitListener(queues = RabbitMqConsts.User.UserAvatarUpdate.QUEUE)
    public void handleAuthorInfoUpdate(AuthorInfoUpdateMessage authorInfoUpdateMessage) {
        log.info("收到作者信息更新消息，作者ID：{}", authorInfoUpdateMessage.getUserId());

        try {
            articleFeignClient.updateAuthorInfo(new ArticleFeignClient.AmsAuthorUpdateDTO(authorInfoUpdateMessage.getUserId(), null, authorInfoUpdateMessage.getAvatar()));

            log.info("已成功更新用戶的作者信息: {}", authorInfoUpdateMessage.getUserId());
        } catch (Exception e) {
            log.error("更新使用者的作者資訊失敗: {}", authorInfoUpdateMessage.getUserId(), e);

            throw e; // 拋出異常以觸發重試機制
        }
    }

    @RabbitListener(queues = RabbitMqConsts.User.ArticleRepliedEmailNotify.QUEUE)
    public void handleArticleRepliedEmailNotify(ArticleRepliedEmailNotifyMessage message) {
        log.info("收到文章回覆 Email 通知消息，articleId={}，recipientUserId={}，replierUserId={}，commentId={}",
                message.getArticleId(), message.getRecipientUserId(), message.getReplierUserId(), message.getCommentId());

        if (message.getRecipientUserId() == null) {
            log.warn("收件人 userId 為空，略過 Email 通知，articleId={}", message.getArticleId());
            return;
        }
        if (message.getRecipientUserId().equals(message.getReplierUserId())) {
            log.info("使用者自行回覆文章，略過 Email 通知，articleId={}", message.getArticleId());
            return;
        }
        if (!isNotificationEnabled(message.getRecipientUserId())) {
            log.info("使用者已關閉通知總開關，略過 Email 通知，recipientUserId={}，articleId={}",
                    message.getRecipientUserId(), message.getArticleId());
            return;
        }

        UmsCredentials credentials = umsCredentialsService.getOne(new LambdaQueryWrapper<UmsCredentials>()
                .select(UmsCredentials::getEmail)
                .eq(UmsCredentials::getUserId, message.getRecipientUserId())
                .last("limit 1"));

        if (credentials == null || StringUtils.isBlank(credentials.getEmail())) {
            log.warn("收件人未設定 Email，略過通知，recipientUserId={}，articleId={}",
                    message.getRecipientUserId(), message.getArticleId());
            return;
        }

        emailService.sendArticleRepliedNotificationEmail(
                credentials.getEmail(),
                message.getArticleTitle(),
                message.getReplierNickname(),
                message.getArticleId(),
                message.getReplyContent(),
                message.getCommentId()
        );
    }

    @RabbitListener(queues = RabbitMqConsts.User.CommentRepliedEmailNotify.QUEUE)
    public void handleCommentRepliedEmailNotify(CommentRepliedEmailNotifyMessage message) {
        log.info("收到留言回覆 Email 通知消息，articleId={}，parentCommentId={}，recipientUserId={}，replierUserId={}，commentId={}",
                message.getArticleId(), message.getParentCommentId(), message.getRecipientUserId(), message.getReplierUserId(), message.getCommentId());

        if (message.getRecipientUserId() == null) {
            log.warn("收件人 userId 為空，略過 Email 通知，parentCommentId={}，commentId={}",
                    message.getParentCommentId(), message.getCommentId());
            return;
        }
        if (message.getRecipientUserId().equals(message.getReplierUserId())) {
            log.info("使用者自行回覆留言，略過 Email 通知，parentCommentId={}，commentId={}",
                    message.getParentCommentId(), message.getCommentId());
            return;
        }
        if (!isNotificationEnabled(message.getRecipientUserId())) {
            log.info("使用者已關閉通知總開關，略過 Email 通知，recipientUserId={}，parentCommentId={}，commentId={}",
                    message.getRecipientUserId(), message.getParentCommentId(), message.getCommentId());
            return;
        }

        UmsCredentials credentials = umsCredentialsService.getOne(new LambdaQueryWrapper<UmsCredentials>()
                .select(UmsCredentials::getEmail)
                .eq(UmsCredentials::getUserId, message.getRecipientUserId())
                .last("limit 1"));

        if (credentials == null || StringUtils.isBlank(credentials.getEmail())) {
            log.warn("收件人未設定 Email，略過通知，recipientUserId={}，parentCommentId={}，commentId={}",
                    message.getRecipientUserId(), message.getParentCommentId(), message.getCommentId());
            return;
        }

        emailService.sendCommentRepliedNotificationEmail(
                credentials.getEmail(),
                message.getArticleTitle(),
                message.getReplierNickname(),
                message.getArticleId(),
                message.getParentCommentId(),
                message.getReplyContent(),
                message.getCommentId()
        );
    }

    private boolean isNotificationEnabled(Long userId) {
        final String cacheKey = RedisCacheKey.USER_NOTIFY_ENABLED.format(userId);

        try {
            RBucket<Integer> bucket = redissonClient.getBucket(cacheKey);
            Integer cached = bucket.get();
            if (cached != null) {
                return cached != 0;
            }
        } catch (Exception e) {
            log.warn("讀取通知總開關快取失敗，將改由資料庫查詢，userId={}", userId, e);
        }

        try {
            UmsUserInfo userInfo = umsUserInfoService.getOne(new LambdaQueryWrapper<UmsUserInfo>()
                    .select(UmsUserInfo::getNotifyEnabled)
                    .eq(UmsUserInfo::getUserId, userId)
                    .last("limit 1"));

            boolean enabled = userInfo == null || userInfo.getNotifyEnabled() == null || userInfo.getNotifyEnabled() != 0;

            try {
                Duration ttl = RedisCacheKey.USER_NOTIFY_ENABLED.getTtl();
                RBucket<Integer> bucket = redissonClient.getBucket(cacheKey);
                if (ttl != null) {
                    bucket.set(enabled ? 1 : 0, ttl);
                } else {
                    bucket.set(enabled ? 1 : 0);
                }
            } catch (Exception e) {
                log.warn("回填通知總開關快取失敗（不影響主流程），userId={}", userId, e);
            }

            return enabled;
        } catch (Exception e) {
            log.warn("查詢使用者通知總開關失敗，將採預設啟用通知，userId={}", userId, e);
            return true;
        }
    }

//    @RabbitListener(queues = RabbitMqConsts.User.ArticleLikedEmailNotify.QUEUE)
//    public void handleArticleLikedEmailNotify(ArticleLikedEmailNotifyMessage message) {
//        log.info("收到文章被點讚 Email 通知消息，articleId={}，authorUserId={}，likedUserId={}",
//                message.getArticleId(), message.getAuthorUserId(), message.getLikedUserId());
//
//        if (message.getAuthorUserId() == null) {
//            log.warn("作者 userId 為空，略過 Email 通知，articleId={}", message.getArticleId());
//            return;
//        }
//        if (message.getAuthorUserId().equals(message.getLikedUserId())) {
//            log.info("作者自行點讚，略過 Email 通知，articleId={}", message.getArticleId());
//            return;
//        }
//
//        UmsCredentials credentials = umsCredentialsService.getOne(new LambdaQueryWrapper<UmsCredentials>()
//                .select(UmsCredentials::getEmail)
//                .eq(UmsCredentials::getUserId, message.getAuthorUserId())
//                .last("limit 1"));
//
//        if (credentials == null || StringUtils.isBlank(credentials.getEmail())) {
//            log.warn("作者未設定 Email，略過通知，authorUserId={}，articleId={}",
//                    message.getAuthorUserId(), message.getArticleId());
//            return;
//        }
//
//        emailService.sendArticleLikedNotificationEmail(
//                credentials.getEmail(),
//                message.getArticleTitle(),
//                message.getLikedUserNickname(),
//                message.getArticleId()
//        );
//    }

}
