package com.shijiawei.secretblog.storage.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.storage.service.SmsMinioService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * ClassName: SmsMinioController
 * Description:
 *
 * @Create 2024/10/27 上午4:21
 */
@Slf4j
@RestController
@RequestMapping("/sms/minio")
public class SmsMinioController {

    @Autowired
    private SmsMinioService smsMinioService;

    @PostMapping
    public R uploadImageToMinio(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("file:{}",file);
        String imgUrl =smsMinioService.uploadImageToMinio(file);
        return R.ok(imgUrl);
    }
}
