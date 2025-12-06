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
 * ClassName: UpdateArticleActionMessage
 * Description: 用於傳遞用戶對文章的互動行為變更（點讚/取消點讚）
 *
 * @Create 2025/12/6 下午6:28
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateArticleActionMessage implements Serializable, RabbitMessage {

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
    private final String exchange = RabbitMqConsts.ams.topicExchange;

    @JsonIgnore
    private final String routingKey = RabbitMqConsts.ams.updateArticleAction.routingKey;
}
