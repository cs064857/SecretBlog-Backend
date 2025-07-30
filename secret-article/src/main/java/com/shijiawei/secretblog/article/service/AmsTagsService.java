package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsTags;

/**
 * ClassName: AmsTagsService
 * Description:
 *
 * @Create 2025/7/28 下午10:29
 */
public interface AmsTagsService extends IService<AmsTags> {
    void createArtTag(String name);
}
