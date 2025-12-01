package com.shijiawei.secretblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import com.shijiawei.secretblog.article.vo.AmsUserCommentVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: AmsCommentMapper
 * Description:
 *
 * @Create 2025/7/16 上午3:03
 */

public interface AmsCommentMapper extends BaseMapper<AmsComment> {

    List<AmsArtCommentStaticVo> getStaticCommentDetails(@Param(value = "articleId") Long articleId);

    /**
     * 根據用戶ID查詢該用戶的留言列表
     * @param userId 用戶ID
     * @return 用戶留言清單
     */
    List<AmsUserCommentVo> selectUserComments(@Param("userId") Long userId);

}
