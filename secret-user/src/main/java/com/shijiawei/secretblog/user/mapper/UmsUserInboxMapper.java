package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsUserInbox;
import org.apache.ibatis.annotations.Mapper;

/**
 * 使用者通知收件匣(Inbox)資料表 Mapper
 */
@Mapper
public interface UmsUserInboxMapper extends BaseMapper<UmsUserInbox> {
}

