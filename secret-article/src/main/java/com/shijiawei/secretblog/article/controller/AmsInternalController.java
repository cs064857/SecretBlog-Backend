package com.shijiawei.secretblog.article.controller;

import com.shijiawei.secretblog.article.dto.AmsAuthorAvatarUpdateDTO;
import com.shijiawei.secretblog.article.dto.AmsAuthorUpdateDTO;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ams/internal/users")
public class AmsInternalController {

    @Autowired
    private AmsArticleService amsArticleService;

    @PutMapping("/{userId}/info")
    public R<Void> updateAuthorInfo(@PathVariable Long userId, @RequestBody AmsAuthorUpdateDTO dto) {
        log.info("Received author info update: userId={}, dto={}", userId, dto);
        amsArticleService.updateAuthorInfo(dto.getUserId(), dto.getNickName(), dto.getAvatar());
        return R.ok();
    }

    @PutMapping("/{userId}/avatar")
    public R<Void> updateAuthorAvatar(@PathVariable Long userId, @RequestBody AmsAuthorAvatarUpdateDTO dto) {
        log.info("Received author avatar update: userId={}, dto={}", userId, dto);
        amsArticleService.updateAuthorAvatar(dto.getUserId(), dto.getAvatar());
        return R.ok();
    }
}
