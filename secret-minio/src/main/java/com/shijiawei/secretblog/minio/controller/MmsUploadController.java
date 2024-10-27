package com.shijiawei.secretblog.minio.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.minio.service.MmsUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: MmsUploadController
 * Description:
 *
 * @Create 2024/10/27 上午4:21
 */
@Slf4j
@RestController
@RequestMapping("/mms")
public class MmsUploadController {


    private MmsUploadService mmsUploadService;

    @PostMapping
    public R minioPostImg(MultipartFile file){
        log.info("file:{}",file);
        return R.ok();
    }
}
