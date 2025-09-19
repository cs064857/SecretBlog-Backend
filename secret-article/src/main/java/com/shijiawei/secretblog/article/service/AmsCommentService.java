package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.R;

import java.util.List;

/**
 * ClassName: AmsCommentService
 * Description:
 *
 * @Create 2025/7/16 上午3:01
 */

public interface AmsCommentService extends IService<AmsComment> {

    R createComment(AmsCommentCreateDTO amsCommentCreateDTO);

    List<AmsArtCommentsVo> getArtComments(Long articleId);

    R<Long> likeComment(Long commentId);

    Boolean existsCommentId(Long commentId);
}
