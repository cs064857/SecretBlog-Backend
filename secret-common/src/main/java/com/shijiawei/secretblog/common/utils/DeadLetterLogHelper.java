package com.shijiawei.secretblog.common.utils;

import org.slf4j.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * 死信隊列日誌處理工具類
 */
public final class DeadLetterLogHelper {

    private DeadLetterLogHelper() {
    }

    public static void log(Logger log, String service, String queue, Message message) {
        if (message == null) {
            log.error("收到死信訊息：Service={}，Queue={}，Message=null", service, queue);
            return;
        }

        MessageProperties properties = message.getMessageProperties();
        if (properties == null) {
            log.error("收到死信訊息：Service={}，Queue={}，MessageProperties=null，Payload={}",
                    service, queue, decodePayload(message));
            return;
        }

        Map<String, Object> headers = properties.getHeaders();
        Object xDeath = headers == null ? null : headers.get("x-death");
        log.error("收到死信訊息：Service={}，Queue={}，Exchange={}，RoutingKey={}，MessageId={}，xDeath={}，Headers={}，Payload={}",
                service,
                queue,
                properties.getReceivedExchange(),
                properties.getReceivedRoutingKey(),
                properties.getMessageId(),
                xDeath,
                headers,
                decodePayload(message));
    }

    private static String decodePayload(Message message) {
        byte[] body = message.getBody();
        return body == null ? null : new String(body, StandardCharsets.UTF_8);
    }
}
