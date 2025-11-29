package com.shijiawei.secretblog.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.shijiawei.secretblog.article.entity.AmsArtAction;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: AmsArtActionMapper
 * Description:
 *
 * @Create 2025/11/25 下午10:09
 */
public interface AmsArtActionMapper extends BaseMapper<AmsArtAction> {

    /**
     * 根據用戶ID查詢點讚過的文章列表
     * @param userId 用戶ID
     * @return 點讚文章列表
     */
    List<UserLikedArticleVo> selectLikedArticlesByUserId(@Param("userId") Long userId);
}
