package com.shijiawei.secretblog.common.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import com.shijiawei.secretblog.common.codeEnum.RabbitMqConsts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * ClassName: AuthorInfoUpdateMessage
 * Description:
 *
 * @Create 2025/12/1 下午6:20
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthorInfoUpdateMessage implements Serializable , RabbitMessage {

    private Long userId;
//    private String nickName;
    private String avatar;


    /**
     * 必填屬性
     */
    private Long timestamp;
    @JsonIgnore
    private final String exchange = RabbitMqConsts.User.TOPIC_EXCHANGE;
    @JsonIgnore
    private final String routingKey = RabbitMqConsts.User.UserAvatarUpdate.ROUTING_KEY;
}
