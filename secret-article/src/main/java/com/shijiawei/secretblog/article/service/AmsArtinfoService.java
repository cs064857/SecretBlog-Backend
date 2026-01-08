package com.shijiawei.secretblog.article.service;

import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsSaveArtInfoVo;

/**
* @author User
* @description 針對表【ams_artInfo】的資料庫操作Service
* @createDate 2024-09-12 04:30:14
*/
public interface AmsArtinfoService extends IService<AmsArtinfo> {

    void saveArticleInfo(AmsSaveArtInfoVo amsSaveArtInfoVo);

    int isArticleOwner(Long articleId, Long userId);

    /**
     * 獲取目前文章的總數量（未刪除的文章）
     * @return 文章總數量
     */
    long getTotalArticleCount();
}
