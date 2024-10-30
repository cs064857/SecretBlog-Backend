package com.shijiawei.secretblog.storage.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * ClassName: SmsMinioService
 * Description:
 *
 * @Create 2024/10/27 下午11:51
 */
public interface SmsMinioService {
    String uploadImageToMinio(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

//    String generatePreSignedUrl() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;
}
