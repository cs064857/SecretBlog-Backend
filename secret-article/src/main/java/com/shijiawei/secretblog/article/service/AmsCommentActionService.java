package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsCommentAction;
import com.shijiawei.secretblog.article.vo.AmsCommentActionVo;

import java.util.List;

/**
 * ClassName: AmsCommentActionService
 * Description: 留言互動記錄 Service 介面
 *
 * @Create 2025/11/26
 */
public interface AmsCommentActionService extends IService<AmsCommentAction> {

    /**
     * 獲取留言互動狀態
     *
     * @param commentId 留言ID
     * @return 留言互動狀態 VO
     */
    AmsCommentActionVo getCommentActionStatusVo(Long commentId);

    /**
     * 獲取文章下所有留言的互動狀態
     *
     * @param articleId 文章ID
     * @return 留言互動狀態 VO 列表
     */
    List<AmsCommentActionVo> getCommentActionStatusVos(Long articleId);
}
