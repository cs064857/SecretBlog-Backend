package com.shijiawei.secretblog.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.user.entity.UmsLocalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地消息表 Mapper
 */
@Mapper
public interface UmsLocalMessageMapper extends BaseMapper<UmsLocalMessage> {

    /**
     * 查詢待發送的本地消息（status = 0 且 next_retry_at <= now）
     */
    @Select("SELECT * FROM ums_local_message " +
            "WHERE status = 0 AND next_retry_at <= #{now} " +
            "ORDER BY id ASC LIMIT #{limit}")
    List<UmsLocalMessage> selectPendingMessages(@Param("now") LocalDateTime now,
                                                @Param("limit") int limit);
}
