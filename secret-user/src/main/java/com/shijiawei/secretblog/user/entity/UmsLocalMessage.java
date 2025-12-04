package com.shijiawei.secretblog.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.shijiawei.secretblog.common.message.BaseLocalMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 本地消息表對應實體
 * 建表: secretblog_ums.ums_local_message
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ums_local_message")
public class UmsLocalMessage extends BaseLocalMessage {

    @Serial
    private static final long serialVersionUID = 1L;
}
