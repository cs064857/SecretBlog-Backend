package com.shijiawei.secretblog.user.rabbit.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.user.entity.UmsCredentials;
import com.shijiawei.secretblog.user.feign.ArticleFeignClient;
import com.shijiawei.secretblog.user.service.EmailService;
import com.shijiawei.secretblog.user.service.UmsCredentialsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
