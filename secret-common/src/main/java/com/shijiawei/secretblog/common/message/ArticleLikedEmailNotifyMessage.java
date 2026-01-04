//package com.shijiawei.secretblog.common.message;
//
//import com.fasterxml.jackson.annotation.JsonIgnore;
//import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
//import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.io.Serializable;
//
///**
// * 文章被點讚後的 Email 通知消息
// *
// */
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class ArticleLikedEmailNotifyMessage implements Serializable, RabbitMessage {
//
//    /**
//     * 作者用戶ID
//     */
//    private Long authorUserId;
//
//    /**
//     * 文章ID
//     */
//    private Long articleId;
//
//    /**
//     * 文章標題（可為空）
//     */
//    private String articleTitle;
//
//    /**
//     * 點讚者用戶ID
//     */
//    private Long likedUserId;
//
//    /**
//     * 點讚者暱稱（可為空）
//     */
//    private String likedUserNickname;
//
//    /**
//     * 訊息時間戳
//     */
//    @Builder.Default
//    private Long timestamp = System.currentTimeMillis();
//
//    @JsonIgnore
//    private final String exchange = RabbitMqConsts.User.TOPIC_EXCHANGE;
//
//    @JsonIgnore
//    private final String routingKey = RabbitMqConsts.User.ArticleLikedEmailNotify.ROUTING_KEY;
//}
//
