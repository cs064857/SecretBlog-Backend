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
 * ClassName: SyncArticleToESMessage
 * Description: 文章同步至 Elasticsearch 消息
 * 用於文章新增或更新時，透過本地消息表 + RabbitMQ 異步同步至 ES
 *
 * @Create 2025/12/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SyncArticleToESMessage implements Serializable, RabbitMessage {

    /**
     * 文章 ID
     */
    private Long articleId;

    /**
     * 操作類型：CREATE（新增）或 UPDATE（更新）
     */
    private String operationType;

    /**
     * 必填屬性：時間戳
     */
    @Builder.Default
    private Long timestamp = System.currentTimeMillis();

    @JsonIgnore
    private final String exchange = RabbitMqConsts.search.topicExchange;

    @JsonIgnore
    private final String routingKey = RabbitMqConsts.search.syncArticleToES.routingKey;

    /**
     * 操作類型常量
     */
    public static final String OPERATION_CREATE = "CREATE";
    public static final String OPERATION_UPDATE = "UPDATE";
}
