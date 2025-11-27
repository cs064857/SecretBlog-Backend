package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsCommentController
 * Description:
 *
 * @Create 2025/7/16 上午2:52
 */
@Slf4j
@RestController
@Tag(name = "文章留言管理", description = "文章留言相關的 CRUD 操作")
@RequestMapping("/article")
public class AmsCommentController {

    @Autowired
    AmsCommentService amsCommentService;
    /**
     * 創建留言
     * @param articleId 文章ID
     * @param amsCommentCreateDTO 留言創建DTO
     * @return 留言創建結果
     */
//    @PostMapping("/comment/create")
    @PostMapping("/{articleId}/comments")
    public R createComment (@PathVariable("articleId") Long articleId,@Validated(value = Insert.class) @RequestBody AmsCommentCreateDTO amsCommentCreateDTO){
        R r = amsCommentService.createComment(articleId,amsCommentCreateDTO);

        return r;
    }

//    /**
//     * 檢查留言是否存在
//     * @param commentId
//     * @return
//     */
//    @GetMapping("/{commentId}")
//    public R<Boolean> getComment(@PathVariable Long commentId){
//
//        log.debug("commentId:{}",commentId);
//        boolean isExistsCommentId = amsCommentService.existsCommentId(commentId);
//        return R.ok(isExistsCommentId);
//
//    }

    /**
     * 取得文章中的所有留言
     * @param articleId
     * @return
     */

    @GetMapping("/{articleId}/comments")
//    @GetMapping("/{articleId}/comments")
    public R<List<AmsArtCommentsVo>> getArtComments(@PathVariable Long articleId){

        log.debug("articleId:{}",articleId);
        List<AmsArtCommentsVo> amsArtCommentsVo = amsCommentService.getArtComments(articleId);
        return R.ok(amsArtCommentsVo);

    }
    /**
     * 點讚留言
     * @param articleId 文章ID
     * @param commentId 留言ID
     * @return 新的點讚數
     */
    @PostMapping("/{articleId}/comments/{commentId}/likes")
    public R<Integer> likeComment(@NotNull @PathVariable(value = "articleId")  Long articleId,@NotNull @PathVariable(value = "commentId") Long commentId){
        log.debug("likeComment - articleId:{}, commentId:{}", articleId, commentId);
        Integer newLikes = amsCommentService.likeComment(articleId,commentId);

        return R.ok(newLikes);
    }

    /**
     * 取消點讚留言
     * @param articleId 文章ID
     * @param commentId 留言ID
     * @return 新的點讚數
     */
    @PostMapping("/{articleId}/comments/{commentId}/unlikes")
    public R<Integer> unlikeComment(@NotNull @PathVariable(value = "articleId")  Long articleId,@NotNull @PathVariable(value = "commentId") Long commentId){
        log.debug("unlikeComment - articleId:{}, commentId:{}", articleId, commentId);
        Integer newLikes = amsCommentService.unlikeComment(articleId, commentId);

        return R.ok(newLikes);
    }
}
