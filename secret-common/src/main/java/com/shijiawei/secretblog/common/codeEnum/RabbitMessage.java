package com.shijiawei.secretblog.common.codeEnum;

/**
 * ClassName: RabbitMessage
 * Description:
 *
 * @Create 2025/12/4 下午3:51
 */
public interface RabbitMessage {

    Long getTimestamp();
    String getExchange();
    String getRoutingKey();

}
