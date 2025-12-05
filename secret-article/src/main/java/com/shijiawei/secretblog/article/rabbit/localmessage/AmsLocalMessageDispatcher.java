package com.shijiawei.secretblog.article.rabbit.localmessage;

import com.shijiawei.secretblog.article.entity.AmsLocalMessage;
import com.shijiawei.secretblog.article.service.AmsLocalMessageService;
import com.shijiawei.secretblog.common.message.BaseLocalMessageDispatcher;
import com.shijiawei.secretblog.common.message.LocalMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Article 模組本地消息調度器
 * 繼承 BaseLocalMessageDispatcher，使用默認調度策略
 * 如需自訂調度策略，可覆寫 dispatch() 方法
 *
 * @Create 2025/12/5 下午10:19
 */
@Component
@RequiredArgsConstructor
public class AmsLocalMessageDispatcher extends BaseLocalMessageDispatcher<AmsLocalMessage> {

    @Autowired
    private AmsLocalMessageService amsLocalMessageService;

    @Override
    protected LocalMessageService<AmsLocalMessage> getLocalMessageService() {
        return amsLocalMessageService;
    }
}
