package com.shijiawei.secretblog.user.rabbit.consumer;

import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.user.feign.ArticleFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: AuthorInfoUpdateConsumer
 * Description:
 *
 * @Create 2025/12/1 下午6:37
 */
@Slf4j
@Component
public class AuthorInfoUpdateConsumer {
    @Autowired
    private ArticleFeignClient articleFeignClient;

    @RabbitListener(queues = "Auth Notification Queue")
    public void handleAuthorInfoUpdate(AuthorInfoUpdateMessage authorInfoUpdateMessage) {
        log.info("收到作者信息更新消息，作者ID：{}", authorInfoUpdateMessage.getUserId());

        try {
            articleFeignClient.updateAuthorInfo(new ArticleFeignClient.AmsAuthorUpdateDTO(authorInfoUpdateMessage.getUserId(), null, authorInfoUpdateMessage.getAvatar()));

            log.info("Successfully updated author info for user: {}", authorInfoUpdateMessage.getUserId());
        } catch (Exception e) {
            log.error("Failed to update author info for user: {}", authorInfoUpdateMessage.getUserId(), e);
            // 這裡可以實現重試邏輯或死信隊列處理
            throw e; // 拋出異常以觸發重試機制
        }
    }

}
