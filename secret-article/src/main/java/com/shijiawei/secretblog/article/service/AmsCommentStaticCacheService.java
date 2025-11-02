package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: AmsCommentStaticCacheService
 * Description:
 *
 * @Create 2025/11/1 下午6:50
 */

public interface AmsCommentStaticCacheService extends IService<AmsComment> {

    List<AmsArtCommentStaticVo> getStaticCommentDetails(Long articleId);

}
