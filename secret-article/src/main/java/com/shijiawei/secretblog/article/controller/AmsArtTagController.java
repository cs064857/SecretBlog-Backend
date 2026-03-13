package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.dto.AssociateTagsToArticleDTO;
import com.shijiawei.secretblog.article.entity.AmsArtTag;
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
@RequestMapping("/ams")
public class AmsArtTagController {

    @Autowired
    private AmsArtTagService amsArtTagService;


    /**
     * 為文章建立標籤關聯
     * @param articleId
     * @param associateTagsToArticleDTO
     * @return
     */
    @PostMapping("/articles/{articleId}/tags")
    public R associateTagsToArticle(@PathVariable Long articleId, @RequestBody AssociateTagsToArticleDTO associateTagsToArticleDTO){
        associateTagsToArticleDTO.setArticleId(articleId);
        amsArtTagService.associateTagsToArticle(associateTagsToArticleDTO);
        return R.ok();
    }

    /**
     * 解除文章與標籤的關聯
     * @param articleId
     * @param associateTagsToArticleDTO
     * @return
     */
    @DeleteMapping("/articles/{articleId}/tags")
    public R unAssociateTagsToArticle(@PathVariable Long articleId, @RequestBody AssociateTagsToArticleDTO associateTagsToArticleDTO){
        associateTagsToArticleDTO.setArticleId(articleId);
        amsArtTagService.unassociateTagsToArticle(associateTagsToArticleDTO);
        return R.ok();
    }

    /**
     * 取得文章關聯的標籤
     * @param articleId
     * @return
     */
    @GetMapping("/articles/{articleId}/tags")
    public R<List<AmsArtTag>> getAssociations(@PathVariable("articleId") Long articleId){
        List<AmsArtTag> amsArtTagList =  amsArtTagService.getAssociationsById(articleId);
        return R.ok(amsArtTagList);
    }
}
