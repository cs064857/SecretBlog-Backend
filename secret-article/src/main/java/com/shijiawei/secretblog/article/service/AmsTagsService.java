package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.entity.AmsTags;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ClassName: AmsTagsService
 * Description:
 *
 * @Create 2025/7/28 下午10:29
 */
public interface AmsTagsService extends IService<AmsTags> {
    void createArtTag(String name);

    List<AmsTags> getArtTags();

    Map<Long, AmsTags> getArtTagsByIds(Set<Long> set);
}
