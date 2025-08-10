package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.R;

/**
 * ClassName: AmsCommentService
 * Description:
 *
 * @Create 2025/7/16 上午3:01
 */

public interface AmsCommentService extends IService<AmsComment> {

    R createComment(AmsCommentCreateDTO amsCommentCreateDTO);

    R<AmsArtCommentsVo> getArtComments(Long articleId);
}
