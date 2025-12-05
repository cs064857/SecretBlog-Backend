package com.shijiawei.secretblog.article.rabbit.consumer;

import com.shijiawei.secretblog.common.message.UpdateArticleLikedMessage;
import com.shijiawei.secretblog.article.service.AmsArtStatusService;
import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ClassName: AmsCunsumer
 * Description:
 *
 * @Create 2025/12/5 上午1:00
 */
@Slf4j
@Component
public class AmsCunsumer {

    @Autowired
    private AmsArtStatusService amsArtStatusService;

    @RabbitListener(queues = RabbitMqConsts.ams.updateArticleLiked.queue)
    public void handleUpdateArticleLiked(UpdateArticleLikedMessage message){
        log.info("RabbitMQ收到更新文章讚數消息，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta());
        try {
            amsArtStatusService.updateLikesCount(message.getArticleId(), message.getDelta());
            log.info("RabbitMQ更新文章讚數成功，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta());
        } catch (Exception e) {
            //日誌記錄異常
            log.error("RabbitMQ更新文章讚數失敗，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta(), e);

            //拋出運行時異常，讓消息隊列知道有問題
            throw new RuntimeException(e);
        }
    }

}
