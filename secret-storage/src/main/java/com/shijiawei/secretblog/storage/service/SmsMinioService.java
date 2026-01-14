package com.shijiawei.secretblog.storage.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * ClassName: SmsMinioService
 * Description:
 *
 * @Create 2024/10/27 下午11:51
 */
public interface SmsMinioService {
    String uploadImageToMinio(MultipartFile file);

    String uploadContentImage(MultipartFile file);

//    String generatePreSignedUrl() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
