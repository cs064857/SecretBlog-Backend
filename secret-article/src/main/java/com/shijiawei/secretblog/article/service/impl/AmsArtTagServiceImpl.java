package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.dto.AssociateTagsToArticleDTO;
import com.shijiawei.secretblog.article.entity.AmsArtTag;
import com.shijiawei.secretblog.article.mapper.AmsArtTagMapper;
import com.shijiawei.secretblog.article.service.AmsArtTagService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * ClassName: AmsArtTagServiceImpl
 * Description:
 *
 * @Create 2025/7/29 上午1:44
 */
@Service
public class AmsArtTagServiceImpl extends ServiceImpl<AmsArtTagMapper, AmsArtTag> implements AmsArtTagService{
    @Override
    public void associateTagsToArticle(AssociateTagsToArticleDTO associateTagsToArticleDTO) {

        AmsArtTag amsArtTag = new AmsArtTag();
        BeanUtils.copyProperties(associateTagsToArticleDTO,amsArtTag);
        this.baseMapper.insert(amsArtTag);

    }

    @Override
    public void unassociateTagsToArticle(AssociateTagsToArticleDTO associateTagsToArticleDTO) {

        this.baseMapper.deleteById(new LambdaQueryWrapper<AmsArtTag>()
                .eq(AmsArtTag::getArticleId,associateTagsToArticleDTO.getArticle_id())
                .in(AmsArtTag::getId,associateTagsToArticleDTO.getTags_id()));

        System.out.println("為文章 " + associateTagsToArticleDTO.getArticle_id() + " 解除了 " + associateTagsToArticleDTO.getTags_id().size() + " 個標籤關聯。");
    }

    @Override
    public List<AmsArtTag> getAssociationsById(Long articleId) {
        List<AmsArtTag> amsArtTags = this.baseMapper.selectList(new LambdaQueryWrapper<AmsArtTag>().eq(AmsArtTag::getArticleId, articleId));

        return amsArtTags;
    }

}
