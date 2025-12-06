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
 * ClassName: UpdateArticleBookmarkActionMessage
 * Description: 使用者對文章書籤行為變更訊息（透過 RabbitMQ 異步同步至資料庫）
 *
 * @Create 2025/12/6
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateArticleBookmarkActionMessage implements Serializable, RabbitMessage {

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用戶ID
     */
    private Long userId;

    /**
     * 書籤狀態 (1: 加入書籤, 0: 移除書籤)
     */
    private Byte isBookmarked;

    /**
     * 訊息時間戳
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    @JsonIgnore
    @Override
    public String getExchange() {
        return RabbitMqConsts.ams.topicExchange;
    }

    @JsonIgnore
    @Override
    public String getRoutingKey() {
        return RabbitMqConsts.ams.updateArticleBookmarkAction.routingKey;
    }
}
