package com.shijiawei.secretblog.article.rabbit.consumer;

import com.shijiawei.secretblog.common.message.*;

import com.shijiawei.secretblog.article.service.AmsArtStatusService;
import com.shijiawei.secretblog.article.service.AmsArtActionService;
import com.shijiawei.secretblog.article.service.AmsCommentStatisticsService;
import com.shijiawei.secretblog.article.service.AmsCommentActionService;
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

    @Autowired
    private AmsArtActionService amsArtActionService;

    @Autowired
    private AmsCommentStatisticsService amsCommentStatisticsService;

    @Autowired
    private AmsCommentActionService amsCommentActionService;

//    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateArticleLiked.QUEUE)
//    public void handleUpdateArticleLiked(UpdateArticleLikedMessage message){
//        log.info("RabbitMQ收到更新文章讚數消息，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta());
//        try {
//            amsArtStatusService.updateLikesCount(message.getArticleId(), message.getDelta());
//            log.info("RabbitMQ更新文章讚數成功，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta());
//        } catch (Exception e) {
//            //日誌記錄異常
//            log.error("RabbitMQ更新文章讚數失敗，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta(), e);
//
//            //拋出運行時異常，讓消息隊列知道有問題
//            throw new RuntimeException(e);
//        }
//    }
//
//    /**
//     * 處理用戶對文章互動行為更新消息（點讚/取消點讚狀態同步到 AmsArtAction）
//     */
//    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateArticleAction.QUEUE)
//    public void handleUpdateArticleAction(UpdateArticleActionMessage message){
//        log.info("RabbitMQ收到更新文章互動行為消息，文章ID: {}, 用戶ID: {}, isLiked: {}",
//                 message.getArticleId(), message.getUserId(), message.getIsLiked());
//        try {
//            amsArtActionService.updateLikedStatus(
//                message.getArticleId(),
//                message.getUserId(),
//                message.getIsLiked()
//            );
//            log.info("RabbitMQ更新文章互動行為成功，文章ID: {}, 用戶ID: {}",
//                     message.getArticleId(), message.getUserId());
//        } catch (Exception e) {
//            log.error("RabbitMQ更新文章互動行為失敗，文章ID: {}, 用戶ID: {}, isLiked: {}",
//                      message.getArticleId(), message.getUserId(), message.getIsLiked(), e);
//            throw new RuntimeException(e);
//        }
//    }


    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateArticleLiked.QUEUE)
    public void handleUpdateArticleLiked(ArticleLikeChangedMessage message){
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

    /**
     * 處理用戶對文章互動行為更新消息（點讚/取消點讚狀態同步到 AmsArtAction）
     */
    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateArticleAction.QUEUE)
    public void handleUpdateArticleAction(ArticleLikeChangedMessage message){
        log.info("RabbitMQ收到更新文章互動行為消息，文章ID: {}, 用戶ID: {}, isLiked: {}",
                message.getArticleId(), message.getUserId(), message.getIsLiked());
        try {
            amsArtActionService.updateLikedStatus(
                    message.getArticleId(),
                    message.getUserId(),
                    message.getIsLiked()
            );
            log.info("RabbitMQ更新文章互動行為成功，文章ID: {}, 用戶ID: {}",
                    message.getArticleId(), message.getUserId());
        } catch (Exception e) {
            log.error("RabbitMQ更新文章互動行為失敗，文章ID: {}, 用戶ID: {}, isLiked: {}",
                    message.getArticleId(), message.getUserId(), message.getIsLiked(), e);
            throw new RuntimeException(e);
        }
    }






    /**
     * 處理文章書籤數更新消息（書籤數同步到 AmsArtStatus）
     */
    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateArticleBookmark.QUEUE)
    public void handleUpdateArticleBookmark(UpdateArticleBookmarkMessage message){
        log.info("RabbitMQ收到更新文章書籤數消息，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta());
        try {
            amsArtStatusService.updateBookmarksCount(message.getArticleId(), message.getDelta());
            log.info("RabbitMQ更新文章書籤數成功，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta());
        } catch (Exception e) {
            log.error("RabbitMQ更新文章書籤數失敗，文章ID: {}，變更量: {}", message.getArticleId(), message.getDelta(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 處理用戶對文章書籤行為更新消息（加入/移除書籤狀態同步到 AmsArtAction）
     */
    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateArticleBookmarkAction.QUEUE)
    public void handleUpdateArticleBookmarkAction(UpdateArticleBookmarkActionMessage message){
        log.info("RabbitMQ收到更新文章書籤行為消息，文章ID: {}, 用戶ID: {}, isBookmarked: {}", 
                 message.getArticleId(), message.getUserId(), message.getIsBookmarked());
        try {
            amsArtActionService.updateBookmarkedStatus(
                message.getArticleId(), 
                message.getUserId(), 
                message.getIsBookmarked()
            );
            log.info("RabbitMQ更新文章書籤行為成功，文章ID: {}, 用戶ID: {}", 
                     message.getArticleId(), message.getUserId());
        } catch (Exception e) {
            log.error("RabbitMQ更新文章書籤行為失敗，文章ID: {}, 用戶ID: {}, isBookmarked: {}", 
                      message.getArticleId(), message.getUserId(), message.getIsBookmarked(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 處理留言讚數更新消息（點讚數同步到 AmsCommentStatistics）
     */
    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateCommentLiked.QUEUE)
    public void handleUpdateCommentLiked(UpdateCommentLikedMessage message){
        log.info("RabbitMQ收到更新留言讚數消息，留言ID: {}，變更量: {}", message.getCommentId(), message.getDelta());
        try {
            amsCommentStatisticsService.updateLikesCount(message.getCommentId(), message.getDelta());
            log.info("RabbitMQ更新留言讚數成功，留言ID: {}，變更量: {}", message.getCommentId(), message.getDelta());
        } catch (Exception e) {
            log.error("RabbitMQ更新留言讚數失敗，留言ID: {}，變更量: {}", message.getCommentId(), message.getDelta(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 處理用戶對留言互動行為更新消息（點讚/取消點讚狀態同步到 AmsCommentAction）
     */
    @RabbitListener(queues = RabbitMqConsts.Ams.UpdateCommentAction.QUEUE)
    public void handleUpdateCommentAction(UpdateCommentActionMessage message){
        log.info("RabbitMQ收到更新留言互動行為消息，留言ID: {}, 用戶ID: {}, isLiked: {}", 
                 message.getCommentId(), message.getUserId(), message.getIsLiked());
        try {
            amsCommentActionService.updateLikedStatus(
                message.getCommentId(),
                message.getArticleId(),
                message.getUserId(), 
                message.getIsLiked()
            );
            log.info("RabbitMQ更新留言互動行為成功，留言ID: {}, 用戶ID: {}", 
                     message.getCommentId(), message.getUserId());
        } catch (Exception e) {
            log.error("RabbitMQ更新留言互動行為失敗，留言ID: {}, 用戶ID: {}, isLiked: {}", 
                      message.getCommentId(), message.getUserId(), message.getIsLiked(), e);
            throw new RuntimeException(e);
        }
    }

}
