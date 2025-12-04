package com.shijiawei.secretblog.user.rabbit.localmessage;

import com.shijiawei.secretblog.common.message.AuthorInfoUpdateMessage;
import com.shijiawei.secretblog.common.message.BaseLocalMessageDispatcher;
import com.shijiawei.secretblog.common.message.LocalMessageService;
import com.shijiawei.secretblog.user.entity.UmsLocalMessage;
import com.shijiawei.secretblog.user.rabbit.producer.AuthorInfoUpdateProducer;
import com.shijiawei.secretblog.user.service.UmsLocalMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * User 服務本地消息調度器
 * 繼承 BaseLocalMessageDispatcher，只需實現消息處理邏輯
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UmsLocalMessageDispatcher extends BaseLocalMessageDispatcher<UmsLocalMessage> {

    private final UmsLocalMessageService umsLocalMessageService;
    private final AuthorInfoUpdateProducer authorInfoUpdateProducer;

    @Override
    protected LocalMessageService<UmsLocalMessage> getLocalMessageService() {
        return umsLocalMessageService;
    }

    @Override
    protected void processMessage(UmsLocalMessage message) throws Exception {
        // 反序列化消息內容
        AuthorInfoUpdateMessage payload = objectMapper.readValue(
                message.getContent(),
                AuthorInfoUpdateMessage.class
        );
        // 發送作者信息更新通知
        authorInfoUpdateProducer.sendAuthorInfoUpdateNotification(payload);
    }

    /**
     * 定時調度待發送的消息
     */
    @Scheduled(fixedDelayString = "${local-message.dispatcher.fixed-delay-ms:5000}")
    public void dispatch() {
        dispatchPendingMessages();
    }
}
