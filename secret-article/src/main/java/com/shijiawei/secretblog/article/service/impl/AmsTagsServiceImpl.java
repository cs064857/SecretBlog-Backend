package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsTags;
import com.shijiawei.secretblog.article.mapper.AmsTagsMapper;
import com.shijiawei.secretblog.article.service.AmsTagsService;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ClassName: AmsTagsServiceImpl
 * Description:
 *
 * @Create 2025/7/28 下午10:28
 */
@Slf4j
@Service
public class AmsTagsServiceImpl extends ServiceImpl<AmsTagsMapper, AmsTags> implements AmsTagsService {
    @Override
    public void createArtTag(String name) {

            AmsTags amsTags = new AmsTags();
            amsTags.setName(name);
            log.info("amsTags:{}",amsTags);


            this.baseMapper.insert(amsTags);
             ///TODO 處理失敗異常

        }
}
