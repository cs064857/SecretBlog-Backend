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
//                    .method(Method.POST)
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
                    "http://192.168.26.5:9000",
                    bucketName,
                    storageName);
            log.info("成功上傳圖片: {}", imgUrl);
            // 將圖片 URL 存入 UMS

            umsUserFeignClient.updateUmsUserAvatar(imgUrl,userId);

            return imgUrl;
        }
        return null;
    }
}
