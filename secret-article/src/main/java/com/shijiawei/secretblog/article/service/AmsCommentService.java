package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.utils.R;

import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * ClassName: AmsCommentService
 * Description:
 *
 * @Create 2025/7/16 上午3:01
 */

public interface AmsCommentService extends IService<AmsComment> {

    R createComment(Long articleId,AmsCommentCreateDTO amsCommentCreateDTO);

    List<AmsArtCommentsVo> getArtComments(Long articleId);

    @OpenCache(prefix = "AmsComments", key = "articleId_#{#articleId}", time = 30, chronoUnit = ChronoUnit.MINUTES)
    List<AmsArtCommentStaticVo> getStaticCommentDetails(Long articleId);

    Long likeComment(Long articleId, Long commentId);

    Boolean existsCommentIdFromDB(Long commentId);
}
