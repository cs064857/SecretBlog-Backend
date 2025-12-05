package com.shijiawei.secretblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.article.entity.AmsLocalMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Article 服務本地消息表 Mapper
 */
@Mapper
public interface AmsLocalMessageMapper extends BaseMapper<AmsLocalMessage> {

    /**
     * 查詢待發送的本地消息（status = 0 且 next_retry_at <= now）
     * @param now 當前時間
     * @param limit 最大筆數
     * @return 待發送消息列表
     */
    @Select("SELECT * FROM ams_local_message " +
            "WHERE status = 0 AND next_retry_at <= #{now} " +
            "ORDER BY id ASC LIMIT #{limit}")
    List<AmsLocalMessage> selectPendingMessages(@Param("now") LocalDateTime now,
                                                @Param("limit") int limit);
}
