package com.shijiawei.secretblog.common.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 留言被回覆（新增子留言）後的 Email 通知消息
 *
 * 由 secret-article 發送，secret-user 消費後寄送 Email 給父留言作者
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRepliedEmailNotifyMessage implements Serializable, RabbitMessage {

    /**
     * 收件人用戶ID（父留言作者）
     */
    private Long recipientUserId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章標題（可為空）
     */
    private String articleTitle;

    /**
     * 父留言ID
     */
    private Long parentCommentId;

    /**
     * 新增的留言ID
     */
    private Long commentId;

    /**
     * 回覆者用戶ID
     */
    private Long replierUserId;

    /**
     * 回覆者暱稱（可為空）
     */
    private String replierNickname;

    /**
     * 回覆內容（可為空）
     */
    private String replyContent;

    /**
     * 訊息時間戳
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    @JsonIgnore
    private final String exchange = RabbitMqConsts.User.TOPIC_EXCHANGE;

    @JsonIgnore
    private final String routingKey = RabbitMqConsts.User.CommentRepliedEmailNotify.ROUTING_KEY;
}

