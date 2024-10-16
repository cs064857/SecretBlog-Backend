package com.shijiawei.secretblog.article.service;

import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.vo.AmsSaveArtInfoVo;

/**
* @author User
* @description 针对表【ams_artInfo】的数据库操作Service
* @createDate 2024-09-12 04:30:14
*/
public interface AmsArtinfoService extends IService<AmsArtinfo> {

    void saveArticleInfo(AmsSaveArtInfoVo amsSaveArtInfoVo);
}
