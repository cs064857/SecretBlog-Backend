package com.shijiawei.secretblog.user.service.Impl;

import com.shijiawei.secretblog.common.codeEnum.RabbitMessage;
import com.shijiawei.secretblog.user.entity.UmsLocalMessage;
import com.shijiawei.secretblog.user.mapper.UmsLocalMessageMapper;
import com.shijiawei.secretblog.user.service.UmsLocalMessageService;
import com.shijiawei.secretblog.common.message.BaseLocalMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * User 服務本地消息服務實現
 */
@Service
@Slf4j
public class UmsLocalMessageServiceImpl
        extends BaseLocalMessageService<UmsLocalMessageMapper, UmsLocalMessage>
        implements UmsLocalMessageService {

    @Override
    public UmsLocalMessage createPendingMessage(UmsLocalMessage localMessage, RabbitMessage message) {
        UmsLocalMessage umsLocalMessage = super.createPendingMessage(localMessage, message);
        this.save(umsLocalMessage);
        return umsLocalMessage;
    }

    /**
     * 查詢待發送的本地消息
     * @param now 當前時間
     * @param limit 最大筆數
     * @return 待發送消息列表
     */
    @Override
    public List<UmsLocalMessage> fetchMessagesToSend(LocalDateTime now, int limit) {
        return this.baseMapper.selectPendingMessages(now, limit);
    }

    // markAsSent 和 markAsFailedAndScheduleRetry 繼承自 BaseLocalMessageService
}
