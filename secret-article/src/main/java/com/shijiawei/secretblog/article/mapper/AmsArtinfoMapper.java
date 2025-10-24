package com.shijiawei.secretblog.article.mapper;

import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
* @author User
* @description 针对表【ams_artInfo】的数据库操作Mapper
* @createDate 2024-09-12 04:30:14
* @Entity com.shijiawei.secretblog.article.entity.AmsArtinfo
*/
public interface AmsArtinfoMapper extends BaseMapper<AmsArtinfo> {

    int isArticleOwner(@Param(value = "articleId") Long articleId, @Param(value = "userId") Long userId);
}




