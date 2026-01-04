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
 * 留言點讚數更新消息
 * 用於將留言點讚數變更同步到 AmsCommentStatistics
 *
 * @Create 2025/12/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentLikedMessage implements Serializable, RabbitMessage {

    /**
     * 留言ID
     */
    private Long commentId;

    /**
     * 變更量（+1 點讚, -1 取消點讚）
     */
    private Integer delta;

    /**
     * 訊息時間戳
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    @JsonIgnore
    private final String exchange = RabbitMqConsts.Ams.TOPIC_EXCHANGE;

    @JsonIgnore
    private final String routingKey = RabbitMqConsts.Ams.UpdateCommentLiked.ROUTING_KEY;
}
