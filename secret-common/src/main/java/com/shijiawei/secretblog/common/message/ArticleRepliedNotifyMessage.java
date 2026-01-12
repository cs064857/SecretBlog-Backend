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
 * 文章被回覆(新增文章留言)後的通知消息(可於 Email 與 Inbox)
 *
 * 由 secret-article 發送，secret-user 消費後可依不同 Queue 執行：
 * 1、寄送 Email 給文章作者
 * 2、寫入通知收件匣(DB/Redis)並推送 SSE
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleRepliedNotifyMessage implements Serializable, RabbitMessage {

    /**
     * 收件人用戶ID(文章作者)
     */
    private Long recipientUserId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 文章標題(可為空)
     */
    private String articleTitle;

    /**
     * 新增的留言ID
     */
    private Long commentId;

    /**
     * 回覆者用戶ID
     */
    private Long replierUserId;

    /**
     * 回覆者暱稱(可為空)
     */
    private String replierNickname;

    /**
     * 回覆者頭像(可為空)
     */
    private String replierAvatar;

    /**
     * 回覆內容(可為空)
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
    private final String routingKey = RabbitMqConsts.User.ArticleRepliedNotify.ROUTING_KEY;
}
