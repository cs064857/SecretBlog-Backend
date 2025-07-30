package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.DTO.AssociateTagsToArticleDTO;
import com.shijiawei.secretblog.article.entity.AmsArtTag;
import com.shijiawei.secretblog.common.utils.R;

import java.util.List;

/**
 * ClassName: AmsArtTagService
 * Description:
 *
 * @Create 2025/7/29 上午1:43
 */
public interface AmsArtTagService extends IService<AmsArtTag> {
    void associateTagsToArticle(AssociateTagsToArticleDTO associateTagsToArticleDTO);

    void unassociateTagsToArticle(AssociateTagsToArticleDTO associateTagsToArticleDTO);

    List<AmsArtTag> getAssociationsById(Long articleId);
}
