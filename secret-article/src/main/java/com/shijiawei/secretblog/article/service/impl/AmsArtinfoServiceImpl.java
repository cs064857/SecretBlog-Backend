package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.shijiawei.secretblog.article.service.AmsArtinfoService;
import com.shijiawei.secretblog.article.mapper.AmsArtinfoMapper;
import com.shijiawei.secretblog.article.vo.AmsSaveArtInfoVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author User
* @description 针对表【ams_artInfo】的数据库操作Service实现
* @createDate 2024-09-12 04:30:14
*/
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
}




