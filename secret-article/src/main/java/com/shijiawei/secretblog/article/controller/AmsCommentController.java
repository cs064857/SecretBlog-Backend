package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.article.dto.AmsCommentEditDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    @Operation(summary = "創建留言", description = "根據文章ID創建留言")
    @ApiResponse(responseCode = "200", description = "成功創建留言")
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
    @Operation(summary = "取得文章中的所有留言", description = "根據文章ID取得所有留言")
    @ApiResponse(responseCode = "200", description = "成功取得文章中的所有留言")
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
    @Operation(summary = "點讚留言", description = "根據文章ID和留言ID點讚")
    @ApiResponse(responseCode = "200", description = "成功點讚留言")
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
    @Operation(summary = "取消點讚留言", description = "根據文章ID和留言ID取消點讚")
    @ApiResponse(responseCode = "200", description = "成功取消點讚留言")
    @PostMapping("/{articleId}/comments/{commentId}/unlikes")
    public R<Integer> unlikeComment(@NotNull @PathVariable(value = "articleId")  Long articleId,@NotNull @PathVariable(value = "commentId") Long commentId){
        log.debug("unlikeComment - articleId:{}, commentId:{}", articleId, commentId);
        Integer newLikes = amsCommentService.unlikeComment(articleId, commentId);

        return R.ok(newLikes);
    }

    /**
     * 刪除留言
     * @param articleId 文章ID
     * @param commentId 留言ID
     * @return 刪除結果
     */

    @Operation(summary = "刪除留言", description = "根據文章ID和留言ID刪除留言")
    @ApiResponse(responseCode = "200", description = "成功刪除留言")
    @PostMapping("/{articleId}/comments/{commentId}")
    public R<Void> deleteComment(@NotNull @PathVariable(value = "articleId") Long articleId,
                          @NotNull @PathVariable(value = "commentId") Long commentId)
    {
        log.debug("deleteComment - articleId:{}, commentId:{}", articleId, commentId);
        return amsCommentService.deleteComment(articleId, commentId);
    }

    /**
     * 編輯留言
     * @param articleId 文章ID
     * @param amsCommentEditDTO 編輯留言DTO
     * @return 編輯結果
     */
    @Operation(summary = "編輯留言", description = "根據文章ID和留言ID編輯留言內容")
    @ApiResponse(responseCode = "200", description = "成功編輯留言")
    @PutMapping("/{articleId}/comments")
    public R<Void> editComment(@NotNull @PathVariable(value = "articleId") Long articleId,
                        @Validated @RequestBody AmsCommentEditDTO amsCommentEditDTO)
    {
        log.debug("editComment - articleId:{}, commentId:{}", articleId, amsCommentEditDTO.getCommentId());
        return amsCommentService.editComment(articleId, amsCommentEditDTO);
    }
}
