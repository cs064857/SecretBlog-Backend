package com.shijiawei.secretblog.user.rabbit.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import com.shijiawei.secretblog.common.message.ArticleRepliedNotifyMessage;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.common.message.CommentRepliedNotifyMessage;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.entity.UmsUser;
import com.shijiawei.secretblog.user.entity.UmsUserInbox;
import com.shijiawei.secretblog.user.feign.ArticleFeignClient;
import com.shijiawei.secretblog.user.service.EmailService;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import com.shijiawei.secretblog.user.service.UmsUserInboxService;
import com.shijiawei.secretblog.user.service.UmsUserInfoService;
import com.shijiawei.secretblog.user.service.UmsUserService;
import com.shijiawei.secretblog.user.utils.AvatarUrlHelper;
import com.shijiawei.secretblog.user.utils.SseClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

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

    @Autowired
    private UmsUserService umsUserService;

    @Autowired
    private UmsUserInboxService umsUserInboxService;

    @Autowired
    private SseClient sseClient;

    @Autowired
    private AvatarUrlHelper avatarUrlHelper;

    @RabbitListener(queues = RabbitMqConsts.User.UserAvatarUpdate.QUEUE)
    public void handleAuthorInfoUpdate(AuthorInfoUpdateMessage authorInfoUpdateMessage) {
        Long userId = authorInfoUpdateMessage == null ? null : authorInfoUpdateMessage.getUserId();
        log.info("收到作者資訊更新訊息，作者 userId={}", userId);

        if (userId == null) {
            log.warn("作者 userId 為空，略過作者資訊同步");
            return;
        }

        String fromAvatar = StringUtils.trimToNull(authorInfoUpdateMessage.getAvatar());
        String fromNickName = null;

        //補齊暱稱(訊息目前只帶 avatar)；同時在訊息未帶 avatar 時，以資料庫為準
        try {
            UmsUser user = umsUserService.getOne(new LambdaQueryWrapper<UmsUser>()
                    .select(UmsUser::getNickName, UmsUser::getAvatar)
                    .eq(UmsUser::getId, userId)
                    .last("limit 1"));
            if (user != null) {
                fromNickName = StringUtils.trimToNull(user.getNickName());
                if (fromAvatar == null) {
                    fromAvatar = StringUtils.trimToNull(user.getAvatar());
                }
            }
        } catch (Exception e) {
            log.warn("查詢作者暱稱/頭像失敗（不影響主流程），userId={}，錯誤：{}", userId, e.getMessage());
        }

        try {
            String storedAvatar = fromAvatar == null ? null : avatarUrlHelper.toStoragePath(fromAvatar);
            String publicAvatar = storedAvatar == null ? null : avatarUrlHelper.toPublicUrl(storedAvatar);

            //收件匣頭像欄位只保存路徑；對外輸出以完整 URL 傳遞
            fromAvatar = storedAvatar;
            articleFeignClient.updateAuthorInfo(new ArticleFeignClient.AmsAuthorUpdateDTO(userId, fromNickName, publicAvatar));

            log.info("已成功同步文章模組作者資訊，userId={}", userId);
        } catch (Exception e) {
            log.error("同步文章模組作者資訊失敗，userId={}", userId, e);

            throw e; // 拋出異常以觸發重試機制
        }

        //同步更新收件匣資料庫
        try {
            LambdaUpdateWrapper<UmsUserInbox> updateWrapper = new LambdaUpdateWrapper<UmsUserInbox>()
                    .eq(UmsUserInbox::getFromUserId, userId)
                    .eq(UmsUserInbox::getDeleted, 0);

            boolean hasSet = false;
            if (fromAvatar != null) {
                updateWrapper.set(UmsUserInbox::getFromAvatar, fromAvatar);
                hasSet = true;
            }
            if (fromNickName != null) {
                updateWrapper.set(UmsUserInbox::getFromNickName, fromNickName);
                hasSet = true;
            }

            if (hasSet) {
                boolean updated = umsUserInboxService.update(updateWrapper);
                log.info("已同步更新收件匣發送者資訊，userId={}，updated={}", userId, updated);
            }
        } catch (Exception e) {
            log.warn("同步更新收件匣發送者資訊失敗（不影響主流程），userId={}，錯誤：{}", userId, e.getMessage());
        }

        //同步更新 Redis 快取(USER_INBOX)
        try {
            if (fromAvatar == null && fromNickName == null) {
                return;
            }

            List<UmsUserInbox> recipients = umsUserInboxService.list(new LambdaQueryWrapper<UmsUserInbox>()
                    .select(UmsUserInbox::getToUserId)
                    .eq(UmsUserInbox::getFromUserId, userId)
                    .eq(UmsUserInbox::getDeleted, 0)
                    .groupBy(UmsUserInbox::getToUserId));

            for (UmsUserInbox row : recipients) {
                Long toUserId = row == null ? null : row.getToUserId();
                if (toUserId == null) {
                    continue;
                }

                String inboxKey = RedisCacheKey.USER_INBOX.format(toUserId);
                RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
                List<UmsUserInbox> cachedList = rList.readAll();
                if (cachedList == null || cachedList.isEmpty()) {
                    continue;
                }

                for (int i = 0; i < cachedList.size(); i++) {
                    UmsUserInbox cached = cachedList.get(i);
                    if (cached == null || cached.getFromUserId() == null) {
                        continue;
                    }
                    if (!userId.equals(cached.getFromUserId())) {
                        continue;
                    }

                    if (fromAvatar != null) {
                        cached.setFromAvatar(fromAvatar);
                    }
                    if (fromNickName != null) {
                        cached.setFromNickName(fromNickName);
                    }
                    rList.set(i, cached);
                }
            }
        } catch (Exception e) {
            log.warn("同步更新收件匣快取發送者資訊失敗（不影響主流程），userId={}，錯誤：{}", userId, e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMqConsts.User.ArticleRepliedInboxNotify.QUEUE)
    public void handleArticleRepliedNotify(ArticleRepliedNotifyMessage message) {
        log.info("收到文章回覆通知訊息，將寫入收件匣並推送 SSE，articleId={}，recipientUserId={}，replierUserId={}，commentId={}",
                message.getArticleId(), message.getRecipientUserId(), message.getReplierUserId(), message.getCommentId());

        //寫入資料庫
        Long recipientUserId = message.getRecipientUserId();
        if (recipientUserId == null) {
            log.warn("收件人 userId 為空，略過通知寫入，articleId={}，commentId={}",
                    message.getArticleId(), message.getCommentId());
            return;
        }

        Long fromUserId = message.getReplierUserId();
        String fromAvatar = StringUtils.trimToNull(message.getReplierAvatar());
        String fromNickName = StringUtils.trimToNull(message.getReplierNickname());

        // 若訊息未帶齊暱稱/頭像，則以使用者資料庫為準（避免前端顯示空白）
        if (fromUserId != null && (fromAvatar == null || fromNickName == null)) {
            try {
                UmsUser user = umsUserService.getOne(new LambdaQueryWrapper<UmsUser>()
                        .select(UmsUser::getNickName, UmsUser::getAvatar)
                        .eq(UmsUser::getId, fromUserId)
                        .last("limit 1"));
                if (user != null) {
                    if (fromNickName == null) {
                        fromNickName = StringUtils.trimToNull(user.getNickName());
                    }
                    if (fromAvatar == null) {
                        fromAvatar = StringUtils.trimToNull(user.getAvatar());
                    }
                }
            } catch (Exception e) {
                log.warn("查詢回覆者暱稱/頭像失敗（不影響主流程），fromUserId={}，錯誤：{}", fromUserId, e.getMessage());
            }
        }

        if (fromAvatar != null) {
            fromAvatar = avatarUrlHelper.toStoragePath(fromAvatar);
        }

        LocalDateTime notifyTime = message.getTimestamp() == null
                ? LocalDateTime.now()
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTimestamp()), ZoneId.systemDefault());

        UmsUserInbox inbox = UmsUserInbox.builder()
                .toUserId(recipientUserId)
                .fromUserId(fromUserId)
                .fromAvatar(fromAvatar)
                .fromNickName(fromNickName)
                .type("ARTICLE_REPLIED")
                .subject(message.getArticleTitle())
                .body(message.getReplyContent())
                .articleId(message.getArticleId())
                .readFlag(0)
                .deleted(0)
                .createAt(notifyTime)
                .updateAt(notifyTime)
                .build();

        try {
            umsUserInboxService.save(inbox);
        } catch (Exception e) {
            log.error("寫入通知收件匣資料庫失敗，recipientUserId={}，commentId={}",
                    recipientUserId, message.getCommentId(), e);
            throw e;
        }

        // 同步寫入 Redis 快取
        try {
            String inboxKey = RedisCacheKey.USER_INBOX.format(recipientUserId);
            RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
            rList.add(inbox);

            Duration ttl = RedisCacheKey.USER_INBOX.getTtl();
            if (ttl != null) {
                rList.expire(ttl);
            }
        } catch (Exception e) {
            log.warn("寫入通知收件匣快取失敗（不影響主流程），recipientUserId={}，commentId={}",
                    recipientUserId, message.getCommentId(), e);
        }

        // 推送至SSE中
        try {
            //SSE對外推送時需輸出完整 URL（資料庫只存路徑）
            inbox.setFromAvatar(avatarUrlHelper.toPublicUrl(inbox.getFromAvatar()));

//            // 構建SSE推送的 JSON payload
//            String ssePayload = String.format(
//                    "{\"articleId\":%d,\"articleTitle\":\"%s\",\"commentId\":%d,\"replierNickname\":\"%s\",\"replierAvatar\":\"%s\",\"replyContent\":\"%s\",\"timestamp\":%d}",
//                    message.getArticleId(),
//                    message.getArticleTitle() != null ? message.getArticleTitle().replace("\"", "\\\"") : "",
//                    message.getCommentId(),
//                    message.getReplierNickname() != null ? message.getReplierNickname().replace("\"", "\\\"") : "",
//                    message.getReplierAvatar() != null ? message.getReplierAvatar().replace("\"", "\\\"") : "",
//                    message.getReplyContent() != null ? message.getReplyContent().replace("\"", "\\\"") : "",
//                    message.getTimestamp() != null ? message.getTimestamp() : System.currentTimeMillis()
//            );

            boolean pushed = sseClient.sendMessage(
                    "ARTICLE_REPLIED",
                    String.valueOf(recipientUserId),
                    String.valueOf(inbox.getId() != null ? inbox.getId() : message.getCommentId()),
                    inbox
            );

            if (pushed) {
                log.info("SSE 推送成功，recipientUserId={}，commentId={}", recipientUserId, message.getCommentId());
            } else {
                log.debug("SSE 推送跳過（使用者未建立連線），recipientUserId={}，commentId={}", recipientUserId, message.getCommentId());
            }
        } catch (Exception e) {
            //SSE推送失敗不影響主流程，僅記錄警告
            log.warn("SSE 推送失敗（不影響主流程），recipientUserId={}，commentId={}，錯誤：{}",
                    recipientUserId, message.getCommentId(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMqConsts.User.ArticleRepliedNotify.QUEUE)
    public void handleArticleRepliedEmailNotify(ArticleRepliedNotifyMessage message) {
        log.info("收到文章回覆 Email 通知訊息，articleId={}，recipientUserId={}，replierUserId={}，commentId={}",
                message.getArticleId(), message.getRecipientUserId(), message.getReplierUserId(), message.getCommentId());

//        if (message.getRecipientUserId() == null) {
//            log.warn("收件人 userId 為空，略過 Email 通知，articleId={}", message.getArticleId());
//            return;
//        }
//        if (message.getRecipientUserId().equals(message.getReplierUserId())) {
//            log.info("使用者自行回覆文章，略過 Email 通知，articleId={}", message.getArticleId());
//            return;
//        }
//        if (!isNotificationEnabled(message.getRecipientUserId())) {
//            log.info("使用者已關閉通知總開關，略過 Email 通知，recipientUserId={}，articleId={}",
//                    message.getRecipientUserId(), message.getArticleId());
//            return;
//        }

        UmsCredentials credentials = umsCredentialsService.getOne(new LambdaQueryWrapper<UmsCredentials>()
                .select(UmsCredentials::getEmail)
                .eq(UmsCredentials::getUserId, message.getRecipientUserId())
                .last("limit 1"));

        if (credentials == null || StringUtils.isBlank(credentials.getEmail())) {
            log.warn("收件人未設定 Email，略過通知，recipientUserId={}，articleId={}",
                    message.getRecipientUserId(), message.getArticleId());
            return;
        }

        Long fromUserId = message.getReplierUserId();
        String fromAvatar = StringUtils.trimToNull(message.getReplierAvatar());
        String fromNickName = StringUtils.trimToNull(message.getReplierNickname());

        if (fromUserId != null && (fromAvatar == null || fromNickName == null)) {
            try {
                UmsUser user = umsUserService.getOne(new LambdaQueryWrapper<UmsUser>()
                        .select(UmsUser::getNickName, UmsUser::getAvatar)
                        .eq(UmsUser::getId, fromUserId)
                        .last("limit 1"));
                if (user != null) {
                    if (fromNickName == null) {
                        fromNickName = StringUtils.trimToNull(user.getNickName());
                    }
                    if (fromAvatar == null) {
                        fromAvatar = StringUtils.trimToNull(user.getAvatar());
                    }
                }
            } catch (Exception e) {
                log.warn("查詢回覆者暱稱/頭像失敗（不影響主流程），fromUserId={}，錯誤：{}", fromUserId, e.getMessage());
            }
        }

        if (fromAvatar != null) {
            fromAvatar = avatarUrlHelper.toStoragePath(fromAvatar);
        }

        emailService.sendArticleRepliedNotificationEmail(
                credentials.getEmail(),
                message.getArticleTitle(),
                fromNickName,
                message.getArticleId(),
                message.getReplyContent(),
                message.getCommentId()
        );

        // 以最佳努力同步回填收件匣發送者資訊（避免收件匣僅存 from_user_id）
        try {
            Long recipientUserId = message.getRecipientUserId();
            if (recipientUserId == null || fromUserId == null) {
                return;
            }
            if (fromAvatar == null && fromNickName == null) {
                return;
            }

            UmsUserInbox latest = umsUserInboxService.getOne(new LambdaQueryWrapper<UmsUserInbox>()
                    .select(UmsUserInbox::getId)
                    .eq(UmsUserInbox::getToUserId, recipientUserId)
                    .eq(UmsUserInbox::getFromUserId, fromUserId)
                    .eq(UmsUserInbox::getType, "ARTICLE_REPLIED")
                    .eq(UmsUserInbox::getDeleted, 0)
                    .orderByDesc(UmsUserInbox::getCreateAt)
                    .last("limit 1"));
            if (latest == null || latest.getId() == null) {
                return;
            }

            UmsUserInbox update = new UmsUserInbox();
            update.setId(latest.getId());
            if (fromAvatar != null) {
                update.setFromAvatar(fromAvatar);
            }
            if (fromNickName != null) {
                update.setFromNickName(fromNickName);
            }
            umsUserInboxService.updateById(update);

            String inboxKey = RedisCacheKey.USER_INBOX.format(recipientUserId);
            RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
            List<UmsUserInbox> cachedList = rList.readAll();
            if (cachedList == null || cachedList.isEmpty()) {
                return;
            }
            for (int i = 0; i < cachedList.size(); i++) {
                UmsUserInbox cached = cachedList.get(i);
                if (cached == null || cached.getId() == null) {
                    continue;
                }
                if (!latest.getId().equals(cached.getId())) {
                    continue;
                }
                if (fromAvatar != null) {
                    cached.setFromAvatar(fromAvatar);
                }
                if (fromNickName != null) {
                    cached.setFromNickName(fromNickName);
                }
                rList.set(i, cached);
                break;
            }
        } catch (Exception e) {
            log.warn("回填收件匣發送者資訊失敗（不影響主流程），recipientUserId={}，commentId={}，錯誤：{}",
                    message.getRecipientUserId(), message.getCommentId(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMqConsts.User.CommentRepliedInboxNotify.QUEUE)
    public void handleCommentRepliedNotify(CommentRepliedNotifyMessage message) {
        log.info("收到留言回覆通知訊息，articleId={}，parentCommentId={}，recipientUserId={}，replierUserId={}，commentId={}",
                message.getArticleId(), message.getParentCommentId(), message.getRecipientUserId(), message.getReplierUserId(), message.getCommentId());

        // 寫入資料庫
        Long recipientUserId = message.getRecipientUserId();
        if (recipientUserId == null) {
            log.warn("收件人 userId 為空，略過通知寫入，articleId={}，parentCommentId={}，commentId={}",
                    message.getArticleId(), message.getParentCommentId(), message.getCommentId());
            return;
        }

        LocalDateTime notifyTime = message.getTimestamp() == null
                ? LocalDateTime.now()
                : LocalDateTime.ofInstant(Instant.ofEpochMilli(message.getTimestamp()), ZoneId.systemDefault());

        UmsUserInbox inbox = UmsUserInbox.builder()
                .toUserId(recipientUserId)
                .fromUserId(message.getReplierUserId())
                .fromAvatar(StringUtils.trimToNull(message.getReplierAvatar()))
                .fromNickName(StringUtils.trimToNull(message.getReplierNickname()))
                .type("COMMENT_REPLIED")
                .subject(message.getArticleTitle())
                .body(message.getReplyContent())
                .articleId(message.getArticleId())
                .readFlag(0)
                .deleted(0)
                .createAt(notifyTime)
                .updateAt(notifyTime)
                .build();

        try {
            umsUserInboxService.save(inbox);
        } catch (Exception e) {
            log.error("寫入通知收件匣資料庫失敗，recipientUserId={}，commentId={}",
                    recipientUserId, message.getCommentId(), e);
            throw e;
        }

        // 同步寫入 Redis 快取
        try {
            String inboxKey = RedisCacheKey.USER_INBOX.format(recipientUserId);
            RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
            rList.add(inbox);

            Duration ttl = RedisCacheKey.USER_INBOX.getTtl();
            if (ttl != null) {
                rList.expire(ttl);
            }
        } catch (Exception e) {
            log.warn("寫入通知收件匣快取失敗（不影響主流程），recipientUserId={}，commentId={}",
                    recipientUserId, message.getCommentId(), e);
        }


        // 推送至SSE中
        try {
            // 構建SSE推送的JSON payload
            String ssePayload = String.format(
                    "{\"articleId\":%d,\"articleTitle\":\"%s\",\"commentId\":%d,\"replierNickname\":\"%s\",\"replierAvatar\":\"%s\",\"replyContent\":\"%s\",\"timestamp\":%d}",
                    message.getArticleId(),
                    message.getArticleTitle() != null ? message.getArticleTitle().replace("\"", "\\\"") : "",
                    message.getCommentId(),
                    message.getReplierNickname() != null ? message.getReplierNickname().replace("\"", "\\\"") : "",
                    message.getReplierAvatar() != null ? message.getReplierAvatar().replace("\"", "\\\"") : "",
                    message.getReplyContent() != null ? message.getReplyContent().replace("\"", "\\\"") : "",
                    message.getTimestamp() != null ? message.getTimestamp() : System.currentTimeMillis()
            );

            boolean pushed = sseClient.sendMessage(
                    "COMMENT_REPLIED",
                    String.valueOf(recipientUserId),
                    String.valueOf(inbox.getId() != null ? inbox.getId() : message.getCommentId()),
                    ssePayload
            );

            if (pushed) {
                log.info("SSE 推送成功，recipientUserId={}，commentId={}", recipientUserId, message.getCommentId());
            } else {
                log.debug("SSE 推送跳過（使用者未建立連線），recipientUserId={}，commentId={}", recipientUserId, message.getCommentId());
            }
        } catch (Exception e) {
            //SSE推送失敗不影響主流程，僅記錄警告
            log.warn("SSE 推送失敗（不影響主流程），recipientUserId={}，commentId={}，錯誤：{}",
                    recipientUserId, message.getCommentId(), e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMqConsts.User.CommentRepliedEmailNotify.QUEUE)
    public void handleCommentRepliedEmailNotify(CommentRepliedNotifyMessage message) {
        log.info("收到留言回覆通知消息，將傳送Email通知，articleId={}，parentCommentId={}，recipientUserId={}，replierUserId={}，commentId={}",
                message.getArticleId(), message.getParentCommentId(), message.getRecipientUserId(), message.getReplierUserId(), message.getCommentId());

        /// TODO判斷使用者是否開啟Email通知

        /**
         * Email 通知
         */
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


//    private boolean isNotificationEnabled(Long userId) {
//        final String cacheKey = RedisCacheKey.USER_NOTIFY_ENABLED.format(userId);
//
//        try {
//            RBucket<Integer> bucket = redissonClient.getBucket(cacheKey);
//            Integer cached = bucket.get();
//            if (cached != null) {
//                return cached != 0;
//            }
//        } catch (Exception e) {
//            log.warn("讀取通知總開關快取失敗，將改由資料庫查詢，userId={}", userId, e);
//        }
//
//        try {
//            UmsUserInfo userInfo = umsUserInfoService.getOne(new LambdaQueryWrapper<UmsUserInfo>()
//                    .select(UmsUserInfo::getNotifyEnabled)
//                    .eq(UmsUserInfo::getUserId, userId)
//                    .last("limit 1"));
//
//            boolean enabled = userInfo == null || userInfo.getNotifyEnabled() == null || userInfo.getNotifyEnabled() != 0;
//
//            try {
//                Duration ttl = RedisCacheKey.USER_NOTIFY_ENABLED.getTtl();
//                RBucket<Integer> bucket = redissonClient.getBucket(cacheKey);
//                if (ttl != null) {
//                    bucket.set(enabled ? 1 : 0, ttl);
//                } else {
//                    bucket.set(enabled ? 1 : 0);
//                }
//            } catch (Exception e) {
//                log.warn("回填通知總開關快取失敗（不影響主流程），userId={}", userId, e);
//            }
//
//            return enabled;
//        } catch (Exception e) {
//            log.warn("查詢使用者通知總開關失敗，將採預設啟用通知，userId={}", userId, e);
//            return true;
//        }
//    }

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
