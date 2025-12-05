package com.shijiawei.secretblog.article.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shijiawei.secretblog.common.message.BaseLocalMessage;
import lombok.*;

import java.io.Serial;

/**
 * 文章服務-本地消息表實體
 * 建表: secretblog_ams.ams_local_message
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ams_local_message")
public class AmsLocalMessage extends BaseLocalMessage {

    @Serial
    private static final long serialVersionUID = 11564545155L;
}
