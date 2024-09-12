package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.shijiawei.secretblog.article.service.AmsArtinfoService;
import com.shijiawei.secretblog.article.vo.AmsSaveArtInfoVo;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName: AmsArtInfoController
 * Description:
 *
 * @Create 2024/9/12 上午4:38
 */
@RestController
@RequestMapping("/article/artInfo")
@Slf4j
public class AmsArtInfoController {

    @Autowired
    private AmsArtinfoService amsArtinfoService;

    @PostMapping
    public R saveArticleInfo(@RequestBody AmsSaveArtInfoVo amsSaveArtInfoVo){
        log.info("amsSaveArtInfoVo:{}", amsSaveArtInfoVo);
        amsArtinfoService.saveArticleInfo(amsSaveArtInfoVo);
        return R.ok();
    }
}
