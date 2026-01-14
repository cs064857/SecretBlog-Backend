package com.shijiawei.secretblog.storage.controller;

import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.storage.service.SmsMinioService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    /**
     * 上傳頭像圖片並更新使用者的頭像
     * @param file 圖片檔案
     * @return 圖片URL

     */
    @PostMapping("/avatar")
    public R uploadImageToMinio(@RequestParam("file") MultipartFile file) {
        log.debug("uploadFileSize:{}",file.getSize());
        String imgUrl = smsMinioService.uploadImageToMinio(file);
//        log.info("imgUrl:{}",imgUrl);
        return R.ok(imgUrl);
    }


    /**
     * 上傳內容圖片(文章、留言等)
     * @param file 圖片檔案
     * @return 圖片URL
     */
    @PostMapping("/content")
    public R uploadContentImage(@RequestParam("file") MultipartFile file) {
        log.debug("uploadContentImage - fileSize: {}", file.getSize());
        String imgUrl = smsMinioService.uploadContentImage(file);
        return R.ok(imgUrl);
    }

}
