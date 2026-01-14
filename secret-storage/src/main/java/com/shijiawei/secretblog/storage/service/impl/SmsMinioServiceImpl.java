package com.shijiawei.secretblog.storage.service.impl;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.feign.dto.UmsUserAvatarUpdateDTO;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import com.shijiawei.secretblog.storage.feignClient.ArticleFeignClient;
import com.shijiawei.secretblog.storage.feignClient.UmsUserFeignClient;
import com.shijiawei.secretblog.storage.service.SmsMinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * ClassName: SmsMinioServiceImpl
 * Description:
 *
 * @Create 2024/10/27 下午11:52
 */
@Service
@Slf4j
//@ConfigurationProperties(prefix = "var.secretblog.bucket.pub")
public class SmsMinioServiceImpl implements SmsMinioService {

    @Autowired
    private MinioClient minioClient;

    @Value("${var.secretblog.bucket.pub.bucketName}")
    private String bucketName;

    @Autowired
    private UmsUserFeignClient umsUserFeignClient;

    @Autowired
    private ArticleFeignClient articleFeignClient;

    @Value("${var.endPoint}")
    private String endpoint;

    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

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


    @Override
    public String uploadImageToMinio(MultipartFile file) {
        //TODO 身分驗證、限制該用戶上傳頻率、限制上傳的文檔類型及大小、CAPTCHA 或人機驗證、IP 黑名單、日誌記錄和監控

        Long userId = getCurrentUserIdOrThrow();
        log.info("用戶上傳圖片 - 用戶ID: {}", userId);

        String imgUrl = uploadImageAndReturnUrl(file, userId, "圖片");

        // 將圖片 URL 存入 UMS
        UmsUserAvatarUpdateDTO umsUserAvatarUpdateDTO = new UmsUserAvatarUpdateDTO(userId, imgUrl);
        R<Void> updatedUmsUserAvatar = umsUserFeignClient.updateUmsUserAvatar(umsUserAvatarUpdateDTO);
        String responseCode = updatedUmsUserAvatar == null ? null : updatedUmsUserAvatar.getCode();
        if (!"200".equals(responseCode)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPLOAD_SYSTEM_ERROR)
                    .detailMessage("在用戶系統中更新用戶頭像失敗")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "originalFilename", StringUtils.defaultString(file == null ? null : file.getOriginalFilename(), ""),
                            "responseCode", StringUtils.defaultString(responseCode, "")
                    ))
                    .build();
        }

//            AmsAuthorAvatarUpdateDTO amsAuthorAvatarUpdateDTO = new AmsAuthorAvatarUpdateDTO(userId, imgUrl);
//            R<Void> updatedArticleAuthorAvatar = articleFeignClient.updateAuthorAvatar(amsAuthorAvatarUpdateDTO);
//            if(!updatedArticleAuthorAvatar.getCode().equals("200")){
//                throw BusinessRuntimeException.builder()
//                        .iErrorCode(ResultCode.UPLOAD_SYSTEM_ERROR)
//                        .detailMessage("在文章系統中更新文章作者頭像失敗")
//                        .data(Map.of(
//                                "userId", ObjectUtils.defaultIfNull(userId, ""),
//                                "originalFilename", StringUtils.defaultString(originalFilename, "")
//                        ))
//                        .build();
//            }

        return imgUrl;
    }

    @Override
    public String uploadContentImage(MultipartFile file) {
        Long userId = getCurrentUserIdOrThrow();
        log.info("用戶上傳內容圖片 - 用戶ID: {}", userId);

        // 不更新用戶頭像，直接返回圖片 URL
        return uploadImageAndReturnUrl(file, userId, "內容圖片");
    }

    private Long getCurrentUserIdOrThrow() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入")
                    .build();
        }
        return userId;
    }

    /**
     * 共用圖片上傳流程
     * 規則：
     * 1、檢查檔案是否為空、類型是否為image/*、大小是否超過限制
     * 2、以UUID產生儲存檔名後上傳至MinIO
     * 3、回傳可公開存取的圖片URL
     */
    private String uploadImageAndReturnUrl(MultipartFile file, Long userId, String imageType) {
        if (file == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("缺少上傳檔案 - 用戶ID: " + userId)
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, "")
                    ))
                    .build();
        }

        if (file.isEmpty()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPLOAD_FILE_EMPTY)
                    .detailMessage("上傳文件不能為空 - 用戶ID: " + userId)
                    .build();
        }

        String fileContentType = file.getContentType();
        log.info("圖片類型:{}", fileContentType);

        // 判斷是否為圖片
        if (!StringUtils.startsWith(fileContentType, "image/")) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPLOAD_FILE_TYPE_INVALID)
                    .detailMessage("上傳文件類型必須為圖片 - 用戶ID: " + userId)
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "contentType", StringUtils.defaultString(fileContentType, ""))
                    )
                    .build();
        }

        long fileSize = file.getSize();
        log.info("圖片大小:{}", fileSize);

        // 檢查圖片大小（最大 5MB）
        if (fileSize > MAX_IMAGE_SIZE_BYTES) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPLOAD_FILE_SIZE_EXCEEDED)
                    .detailMessage("上傳文件大小不能超過5MB")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "contentType", StringUtils.defaultString(fileContentType, ""),
                            "fileSize", ObjectUtils.defaultIfNull(fileSize, "")
                    ))
                    .build();
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = StringUtils.substringAfterLast(StringUtils.defaultString(fileContentType, ""), "/"); // image/png => png
        log.info("文件副檔名:{}", fileExtension);

        if (StringUtils.isNotBlank(originalFilename) && StringUtils.isNotBlank(fileExtension)) {
            String storageName = UUID.randomUUID().toString().concat(".").concat(fileExtension);
            try {
                minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(storageName)
                        .contentType(fileContentType)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build());
            } catch (Exception e) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.UPLOAD_SYSTEM_ERROR)
                        .detailMessage("文件上傳失敗")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId, ""),
                                "originalFilename", StringUtils.defaultString(originalFilename, ""),
                                "error", e.getMessage()
                        ))
                        .cause(e)
                        .build();
            }

            String imgUrl = String.format("%s/%s/%s", endpoint, bucketName, storageName);
            log.info("成功上傳{}: {}", imageType, imgUrl);
            return imgUrl;
        }

        throw BusinessRuntimeException.builder()
                .iErrorCode(ResultCode.UPLOAD_FILE_INVALID)
                .detailMessage("上傳文件名稱無效 - 用戶ID: " + userId)
                .data(Map.of(
                        "userId", ObjectUtils.defaultIfNull(userId, ""),
                        "originalFilename", StringUtils.defaultString(originalFilename, ""),
                        "suffix", StringUtils.defaultString(fileExtension, "")
                ))
                .build();
    }
}
