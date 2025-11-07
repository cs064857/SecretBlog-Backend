package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
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
@RequestMapping("/article")
public class AmsCommentController {

    @Autowired
    AmsCommentService amsCommentService;
    /**
     * 創建評論
     * @param amsCommentCreateDTO
     * @return
     */
//    @PostMapping("/comment/create")
    @PostMapping("/{articleId}/comments")
    public R createComment (@PathVariable("articleId") Long articleId,@Validated(value = Insert.class) @RequestBody AmsCommentCreateDTO amsCommentCreateDTO){
        R r = amsCommentService.createComment(articleId,amsCommentCreateDTO);

        return r;
    }

//    /**
//     * 檢查評論是否存在
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
     * 取得文章中的所有評論
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
     * 點讚評論
     * @param commentId
     * @return
     */

    @PostMapping("/{articleId}/comments/{commentId}/likes")
    public R<Integer> likeComment(@NotNull @PathVariable(value = "articleId")  Long articleId,@NotNull @PathVariable(value = "commentId") Long commentId){
        log.debug("commentId:{}",commentId);
        Integer newLikes = amsCommentService.likeComment(articleId,commentId);

        return R.ok(newLikes);
    }
}
