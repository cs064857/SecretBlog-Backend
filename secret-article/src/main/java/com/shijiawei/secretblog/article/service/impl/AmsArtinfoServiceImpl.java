package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.shijiawei.secretblog.article.service.AmsArtinfoService;
import com.shijiawei.secretblog.article.mapper.AmsArtinfoMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArtInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author User
* @description 針對表【ams_artInfo】的資料庫操作Service实现
* @createDate 2024-09-12 04:30:14
*/
@Slf4j
@Service
public class AmsArtinfoServiceImpl extends ServiceImpl<AmsArtinfoMapper, AmsArtinfo>
    implements AmsArtinfoService{

    @Override
    public void saveArticleInfo(AmsSaveArtInfoVo amsSaveArtInfoVo) {
        AmsArtinfo amsArtinfo = new AmsArtinfo();
        BeanUtils.copyProperties(amsSaveArtInfoVo,amsArtinfo);
        this.baseMapper.insert(amsArtinfo);
    }

    @Override
    public int isArticleOwner(Long articleId, Long userId) {

        return this.baseMapper.isArticleOwner(articleId, userId);

    }

    /**
     * 獲取目前文章的總數量（未刪除文章的不重複 articleId 數量）
     * @return 不重複的文章 ID 數量
     */
    @Override
    public long getTotalArticleCount() {
        log.debug("開始獲取文章總數量 (不重複 articleId)");
        long count = this.list()
                .stream()
                .map(AmsArtinfo::getArticleId)
                .distinct()
                .count();
        log.debug("文章總數量查詢完成，distinctArticleIdCount={}", count);
        return count;
    }

}




