package com.shijiawei.secretblog.storage.service.impl;

import com.shijiawei.secretblog.storage.feignClient.UmsUserFeignClient;
import com.shijiawei.secretblog.storage.service.SmsMinioService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * ClassName: SmsMinioServiceImpl
 * Description:
 *
 * @Create 2024/10/27 下午11:52
 */
@Service
@Slf4j
@ConfigurationProperties(prefix = "var.secretblog")
public class SmsMinioServiceImpl implements SmsMinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${var.secretblog.bucketName}")
    private String bucketName;

    @Autowired
    private UmsUserFeignClient umsUserFeignClient;

//    /**
//     * 生成預簽名URL
//     * @return
//     * @throws ServerException
//     * @throws InsufficientDataException
//     * @throws ErrorResponseException
//     * @throws IOException
//     * @throws NoSuchAlgorithmException
//     * @throws InvalidKeyException
//     * @throws InvalidResponseException
//     * @throws XmlParserException
//     * @throws InternalException
//     */
//    @Override
//    public String generatePreSignedUrl() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
//
//
//            String storageName= UUID.randomUUID().toString();
//            String presignedObjectUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
//                    .bucket(bucketName)
//                    .object(storageName)
//                    .method(Method.PUT)
//                    .expiry(30, TimeUnit.MINUTES)
//                    .build());
//
//            if(StringUtils.isNotEmpty(presignedObjectUrl)){
//                log.info("成功生成預簽名 URL: {}", presignedObjectUrl);
//                return presignedObjectUrl;
//            }
//
//        return null;
//    }

//    /**
//     * 限制預簽名URL上傳次數
//     * @param urlId
//     * @return
//     */
//    public boolean checkAndUpdateUrlUsage(String urlId) {
//        return urlUsageCount.computeIfPresent(urlId, (key, count) -> {
//            if (count > 0) {
//                log.info("URL使用次數更新: {}", count - 1);
//                return count - 1;
//            }
//            log.warn("URL已達到最大使用次數");
//            return 0;
//        }) != null;
//    }

    /**
     * 上傳圖片
     * @param file
     * @return
     * @throws ServerException
     * @throws InsufficientDataException
     * @throws ErrorResponseException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws InvalidResponseException
     * @throws XmlParserException
     * @throws InternalException
     */
    @Override
    public String uploadImageToMinio(MultipartFile file,String userId) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //TODO 身分驗證、限制該用戶上傳頻率、限制上傳的文檔類型及大小、CAPTCHA 或人機驗證、IP 黑名單、日誌記錄和監控
        if(file.isEmpty()){
            throw new RuntimeException("圖片不存在");
        };
        log.info("file.getContentType():{}",file.getContentType());

        //判斷是否為圖片,若不為圖片則拋出異常
        if(!Objects.requireNonNull(file.getContentType()).startsWith("image/")){
            throw new RuntimeException("並非圖片類型");
        };
        log.info("file.getSize():{}",file.getSize());
        //若圖片大小大於5MB,則拋出異常
        if(file.getSize()>1024*1024*5){
            throw new RuntimeException("圖片必須小於5MB");
        };
        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFilename)) {
            String storageName = UUID.randomUUID().toString();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storageName)
                    .contentType(file.getContentType())
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .build());

            String imgUrl =String.format("%s/api/v1/buckets/%s/objects/download?preview=true&prefix=%s&version_id=null",
                    "http://4.240.82.138:9000",
                    bucketName,
                    storageName);
            log.info("成功上傳圖片: {}", imgUrl);
            // 將圖片 URL 存入 UMS
            if(StringUtils.isNotEmpty(userId)){
                umsUserFeignClient.updateUmsUserAvatar(imgUrl,userId);
            }


            return imgUrl;
        }
        return null;
    }
}
