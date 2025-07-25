package com.shijiawei.secretblog.storage.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.storage.service.SmsMinioService;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
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

//    @GetMapping
//    public R getPreSignedUrlFromMinio() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//        ///TODO 身分驗證
//        String preSignedUrl =smsMinioService.generatePreSignedUrl();
//        return R.ok(preSignedUrl);
//    }

    @PostMapping
    public R uploadImageToMinio(@RequestParam("file") MultipartFile file,String userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        log.info("file:{}",file);
        log.info("userId:{}",userId);
        String imgUrl =smsMinioService.uploadImageToMinio(file,userId);
        log.info("imgUrl:{}",imgUrl);
        return R.ok(imgUrl);
    }


}
