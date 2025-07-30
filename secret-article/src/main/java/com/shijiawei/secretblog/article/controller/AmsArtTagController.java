package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.DTO.AssociateTagsToArticleDTO;
import com.shijiawei.secretblog.article.entity.AmsArtTag;
import com.shijiawei.secretblog.article.entity.AmsTags;
import com.shijiawei.secretblog.article.service.AmsArtTagService;
import com.shijiawei.secretblog.common.utils.R;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsArtTag
 * Description:
 *
 * @Create 2025/7/28 下午11:08
 */
@Slf4j
@RestController
@RequestMapping("/article/tag")
public class AmsArtTagController {

    @Autowired
    private AmsArtTagService amsArtTagService;

    @PostMapping("/associate")
    public R associateTagsToArticle(@RequestBody AssociateTagsToArticleDTO associateTagsToArticleDTO){

        amsArtTagService.associateTagsToArticle(associateTagsToArticleDTO);
        return R.ok();
    }
    @PostMapping("/unassociate")
    public R unassociateTagsToArticle(@RequestBody AssociateTagsToArticleDTO associateTagsToArticleDTO){

        amsArtTagService.unassociateTagsToArticle(associateTagsToArticleDTO);
        return R.ok();
    }
    @GetMapping("/associations/{article_id}")
    public R<List<AmsArtTag>> getassociations(@PathVariable("article_id") Long articleId){

        List<AmsArtTag> amsArtTagList =  amsArtTagService.getAssociationsById(articleId);
        return R.ok(amsArtTagList);
    }
}
