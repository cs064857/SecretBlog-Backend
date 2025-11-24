package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.dto.AmsTagsCreateArtDTO;
import com.shijiawei.secretblog.article.dto.AmsTagsDeleteDTO;
import com.shijiawei.secretblog.article.entity.AmsTags;
import com.shijiawei.secretblog.article.service.AmsTagsService;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.ValidationGroups;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * ClassName: AmsTagsController
 * Description:
 *
 * @Create 2025/7/28 下午10:29
 */
@Slf4j
@Tag(name = "標籤管理", description = "文章標籤相關的 CRUD 操作")
@RequestMapping("/article/tags")
@RestController
public class AmsTagsController {

    @Autowired
    private AmsTagsService amsTagsService;

    /**
     * 新增標籤
     */
    @Operation(
            summary = "新增標籤",
            description = "在資料庫中新增一個文章標籤"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "成功新增標籤"
            )
    })
    @PostMapping("/create")
    public R createArtTag(@Validated(value = {ValidationGroups.Insert.class}) @RequestBody AmsTagsCreateArtDTO createArtTagDTO){

        amsTagsService.createArtTag(createArtTagDTO.getName());
        log.info("name:{}",createArtTagDTO.getName());
        return R.ok();
    }

    /**
     * 取得所有標籤
     */
    @Operation(
            summary = "取得所有標籤",
            description = "查詢資料庫中所有的文章標籤,無分頁限制"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "成功取得標籤列表"
            ),
    })
    @GetMapping("/list")
    public R<List<AmsTags>> getArtTags(){
        List<AmsTags> amsTagsList = amsTagsService.getArtTags();
        return R.ok(amsTagsList);
    }
    /**
     * 移除標籤
     */
    @Operation(
            summary = "刪除標籤",
            description = "從資料庫中移除標籤"
    )
    @ApiResponse(
            responseCode = "200",
            description = "成功刪除標籤"
    )
    @PostMapping("/delete/{id}")
    public R deleteArtTag(@Validated(value = {ValidationGroups.Delete.class}) @PathVariable(value = "id") Long id){

        boolean removed = amsTagsService.removeById(id);
        if(removed){
            return R.ok();
        }
//        throw new CustomRuntimeException("標籤刪除失敗");
        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                .detailMessage("標籤刪除失敗")
                .data(Map.of("id", ObjectUtils.defaultIfNull(id, "")))
                .build();

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
