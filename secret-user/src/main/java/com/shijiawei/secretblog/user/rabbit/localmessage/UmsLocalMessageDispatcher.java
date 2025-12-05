package com.shijiawei.secretblog.user.rabbit.localmessage;

import com.shijiawei.secretblog.common.message.BaseLocalMessageDispatcher;
import com.shijiawei.secretblog.common.message.LocalMessageService;
import com.shijiawei.secretblog.user.entity.UmsLocalMessage;
import com.shijiawei.secretblog.user.service.UmsLocalMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * User 模組本地消息調度器
 * 繼承 BaseLocalMessageDispatcher，使用默認調度策略
 * 如需自訂調度策略，可覆寫 dispatch() 方法
 */
@Component
@RequiredArgsConstructor
public class UmsLocalMessageDispatcher extends BaseLocalMessageDispatcher<UmsLocalMessage> {

    private final UmsLocalMessageService umsLocalMessageService;

    @Override
    protected LocalMessageService<UmsLocalMessage> getLocalMessageService() {
        return umsLocalMessageService;
    }
}
