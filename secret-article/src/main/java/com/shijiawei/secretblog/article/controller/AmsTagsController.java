package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.dto.AmsTagsCreateArtDTO;
import com.shijiawei.secretblog.article.dto.AmsTagsDeleteDTO;
import com.shijiawei.secretblog.article.entity.AmsTags;
import com.shijiawei.secretblog.article.service.AmsTagsService;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ClassName: AmsTagsController
 * Description:
 *
 * @Create 2025/7/28 下午10:29
 */
@Slf4j
@RequestMapping("/article/tags")
@RestController
public class AmsTagsController {

    @Autowired
    private AmsTagsService amsTagsService;

    /**
     * 新增標籤
     */
    @PostMapping("/create")
    public R createArtTag(@Validated(value = {ValidationGroups.Insert.class}) @RequestBody AmsTagsCreateArtDTO createArtTagDTO){

        amsTagsService.createArtTag(createArtTagDTO.getName());
        log.info("name:{}",createArtTagDTO.getName());
        return R.ok();
    }

    /**
     * 列出所有標籤
     * @return
     */
    @GetMapping("/list")
    public R<List<AmsTags>> getArtTags(){
        List<AmsTags> amsTagsList = amsTagsService.list();
        return R.ok(amsTagsList);
    }
    /**
     * 移除標籤
     */
    @PostMapping("/delete/{id}")
    public R deleteArtTag(@Validated(value = {ValidationGroups.Delete.class}) @PathVariable(value = "id") Long id){

        amsTagsService.removeById(id);

        return R.ok();
    }
    /**
     * 移除標籤
     */
    @PostMapping("/batch-delete")
    public R deleteArtTags(@Validated(value = {ValidationGroups.Delete.class}) @RequestBody AmsTagsDeleteDTO amsTagsDeleteDTO){

        amsTagsService.removeByIds(amsTagsDeleteDTO.getIds());

        return R.ok();
    }
}
