package com.shijiawei.secretblog.common.codeEnum;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.common.message.UpdateArticleLikedMessage;

/**
 * RabbitMQ 消息通用介面
 * 使用 Jackson 多態類型處理，支援反序列化多種消息類型
 *
 * @Create 2025/12/4 下午3:51
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "@type",
        defaultImpl = AuthorInfoUpdateMessage.class  // 預設實現，兼容舊數據
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AuthorInfoUpdateMessage.class, name = "authorInfoUpdate"),
        @JsonSubTypes.Type(value = UpdateArticleLikedMessage.class, name = "updateArticleLiked")
        // 添加更多消息類型
        // @JsonSubTypes.Type(value = OtherMessage.class, name = "other")
})
public interface RabbitMessage {

    Long getTimestamp();
    String getExchange();
    String getRoutingKey();

}
