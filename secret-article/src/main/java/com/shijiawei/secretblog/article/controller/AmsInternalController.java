package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.dto.AmsAuthorAvatarUpdateDTO;
import com.shijiawei.secretblog.article.dto.AmsAuthorUpdateDTO;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/article/internal/user")
public class AmsInternalController {

    @Autowired
    private AmsArticleService amsArticleService;

    @PostMapping("/update-info")
    public R<Void> updateAuthorInfo(@RequestBody AmsAuthorUpdateDTO dto) {
        log.info("Received author info update: {}", dto);
        amsArticleService.updateAuthorInfo(dto.getUserId(), dto.getNickName(), dto.getAvatar());
        return R.ok();
    }

    @PostMapping("/update-avatar")
    public R<Void> updateAuthorAvatar(@RequestBody AmsAuthorAvatarUpdateDTO dto) {
        log.info("Received author avatar update: {}", dto);
        amsArticleService.updateAuthorAvatar(dto.getUserId(), dto.getAvatar());
        return R.ok();
    }
}
