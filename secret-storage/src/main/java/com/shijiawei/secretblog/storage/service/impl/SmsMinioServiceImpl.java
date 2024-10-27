package com.shijiawei.secretblog.storage.service.impl;

import com.shijiawei.secretblog.storage.service.SmsMinioService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

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

//    @Value("${var.secretblog.bucketName}")
    private String bucketName;

    /**
     * 生成預簽名URL
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
    public String generatePreSignedUrl() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {


            String storageName= UUID.randomUUID().toString();
            String presignedObjectUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .build());

            if(StringUtils.isNotEmpty(presignedObjectUrl)){
                log.info("成功生成預簽名 URL: {}", presignedObjectUrl);
                return presignedObjectUrl;
            }

        return null;
    }

    /**
     * 限制預簽名URL上傳次數
     * @param urlId
     * @return
     */
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
    public String uploadImageToMinio(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        System.out.println();
//        String name = file.getName();

        String originalFilename = file.getOriginalFilename();
        if (StringUtils.isNotBlank(originalFilename)) {
            String[] split = originalFilename.split("\\.");//獲取文件副檔名
            String storageName= UUID.randomUUID().toString()+"."+split[1];
            String presignedObjectUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(storageName)
                    .build());

            if(StringUtils.isNotEmpty(presignedObjectUrl)){
                log.info("成功生成預簽名 URL: {}", presignedObjectUrl);
                return presignedObjectUrl;
            }
        }
        return null;
    }

}
