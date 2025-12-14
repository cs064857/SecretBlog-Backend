package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.vo.AmsUserCommentVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.article.dto.AmsCommentEditDTO;
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


    IPage<AmsUserCommentVo> getUserCommentsByUserId(Long userId, Integer routePage);

    @OpenCache(prefix = "AmsComments", key = "articleId_#{#articleId}", time = 30, chronoUnit = ChronoUnit.MINUTES)
    List<AmsArtCommentStaticVo> getStaticCommentDetails(Long articleId);

    Integer likeComment(Long articleId, Long commentId);

    Integer unlikeComment(Long articleId, Long commentId);

    Boolean existsCommentIdFromDB(Long commentId);

    R<Void> deleteComment(Long articleId, Long commentId);

    R<Void> editComment(Long articleId, AmsCommentEditDTO amsCommentEditDTO);
}
