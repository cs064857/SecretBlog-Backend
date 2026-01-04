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
 * 用戶對留言互動行為更新消息
 * 用於將點讚/取消點讚狀態同步到 AmsCommentAction
 *
 * @Create 2025/12/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentActionMessage implements Serializable, RabbitMessage {

    /**
     * 留言ID
     */
    private Long commentId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用戶ID
     */
    private Long userId;

    /**
     * 點讚狀態 (1: 點讚, 0: 取消點讚)
     */
    private Byte isLiked;

    /**
     * 訊息時間戳
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    @JsonIgnore
    private final String exchange = RabbitMqConsts.Ams.TOPIC_EXCHANGE;

    @JsonIgnore
    private final String routingKey = RabbitMqConsts.Ams.UpdateCommentAction.ROUTING_KEY;
}
