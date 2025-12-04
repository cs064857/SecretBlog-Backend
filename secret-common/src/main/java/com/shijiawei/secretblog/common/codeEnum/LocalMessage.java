package com.shijiawei.secretblog.common.codeEnum;

import java.time.LocalDateTime;

/**
 * ClassName: LocalMessage
 * Description:
 *
 * @Create 2025/12/4 下午3:58
 */
public interface LocalMessage {

    /**
     * Getters
     */
    Long getId();
    String getMsgId();
    String getExchange();
    String getRoutingKey();
    String getContent();
    Integer getStatus();
    Integer getRetryCount();
    LocalDateTime getNextRetryAt();
    LocalDateTime getCreateAt();
    LocalDateTime getUpdateAt();
    String getErrorMsg();

    /**
     * Setters
     */
    void setId(Long id);
    void setMsgId(String msgId);
    void setExchange(String exchange);
    void setRoutingKey(String routingKey);
    void setContent(String content);
    void setStatus(Integer status);
    void setRetryCount(Integer retryCount);
    void setNextRetryAt(LocalDateTime nextRetryAt);
    void setCreateAt(LocalDateTime createAt);
    void setUpdateAt(LocalDateTime updateAt);
    void setErrorMsg(String errorMsg);
}
