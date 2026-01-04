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
// * ClassName: UpdateArticleLikedMessage
// * Description:
// *
// * @Create 2025/12/5 上午1:08
// */
//@Data
//@Builder
//@AllArgsConstructor
//@NoArgsConstructor
//public class UpdateArticleLikedMessage implements Serializable , RabbitMessage {
//
//    private Long articleId;
//    private Integer delta;//變更量
//
//    /**
//     * 必填屬性
//     */
//    @Builder.Default
//    private Long timestamp = System.currentTimeMillis();
//    @JsonIgnore
//    private final String exchange = RabbitMqConsts.Ams.TOPIC_EXCHANGE;
//    @JsonIgnore
//    private final String routingKey = RabbitMqConsts.Ams.UpdateArticleLiked.ROUTING_KEY;
//}
