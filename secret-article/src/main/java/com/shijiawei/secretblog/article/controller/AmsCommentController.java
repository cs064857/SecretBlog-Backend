package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.vaildation.Insert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * ClassName: AmsCommentController
 * Description:
 *
 * @Create 2025/7/16 上午2:52
 */
@Slf4j
@RestController
@RequestMapping("/article/comment")
public class AmsCommentController {

    @Autowired
    AmsCommentService amsCommentService;

    @PostMapping("/create")
    public R createComment (@Validated(value = Insert.class) @RequestBody AmsCommentCreateDTO amsCommentCreateDTO){
        R r = amsCommentService.createComment(amsCommentCreateDTO);

        return r;
    }


}
