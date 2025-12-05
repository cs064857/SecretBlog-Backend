package com.shijiawei.secretblog.article.service.impl;

import com.shijiawei.secretblog.article.entity.AmsLocalMessage;
import com.shijiawei.secretblog.article.mapper.AmsLocalMessageMapper;
import com.shijiawei.secretblog.article.service.AmsLocalMessageService;
import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import com.shijiawei.secretblog.common.message.BaseLocalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Article 服務本地消息服務實現
 */
@Service
@Slf4j
public class AmsLocalMessageServiceImpl
        extends BaseLocalMessageService<AmsLocalMessageMapper, AmsLocalMessage>
        implements AmsLocalMessageService {

    @Override
    protected AmsLocalMessage getLocalMessage() {
        return new AmsLocalMessage();
    }

    @Override
    public AmsLocalMessage createPendingMessage(RabbitMessage message) {
        AmsLocalMessage amsLocalMessage = super.createPendingMessage(message);
        this.save(amsLocalMessage);
        return amsLocalMessage;
    }

    /**
     * 查詢待發送的本地消息
     * @param now 當前時間
     * @param limit 最大筆數
     * @return 待發送消息列表
     */
    @Override
    public List<AmsLocalMessage> fetchMessagesToSend(LocalDateTime now, int limit) {
        return this.baseMapper.selectPendingMessages(now, limit);
    }

    // markAsSent 和 markAsFailedAndScheduleRetry 繼承自 BaseLocalMessageService
}
