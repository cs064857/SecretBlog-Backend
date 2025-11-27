package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.service.AmsCommentActionService;
import com.shijiawei.secretblog.article.vo.AmsCommentActionVo;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsCommentActionController
 * Description: 留言互動記錄 Controller
 *
 * @Create 2025/11/26
 */

@Slf4j
@RequestMapping("/article")
@RestController
public class AmsCommentActionController {

    @Autowired
    private AmsCommentActionService amsCommentActionService;

//    /**
//     * 獲取留言互動狀態
//     *
//     * @param commentId 留言ID
//     * @return 留言互動狀態
//     */
//    @GetMapping("/comments/{commentId}/action-status")
//    public R<AmsCommentActionVo> getCommentAction(@PathVariable(value = "commentId") Long commentId) {
//        AmsCommentActionVo amsCommentActionVo = amsCommentActionService.getCommentActionStatusVo(commentId);
//        return R.ok(amsCommentActionVo);
//    }

    /**
     * 獲取文章下所有留言的互動狀態
     *
     * @param articleId 文章ID
     * @return 留言互動狀態列表
     */
    @GetMapping("/comments/{articleId}/action-status")
    public R<List<AmsCommentActionVo>> getCommentActionStatusVos(@PathVariable(value = "articleId") Long articleId) {
        List<AmsCommentActionVo> amsCommentActionVoList = amsCommentActionService.getCommentActionStatusVos(articleId);
        return R.ok(amsCommentActionVoList);
    }

}
