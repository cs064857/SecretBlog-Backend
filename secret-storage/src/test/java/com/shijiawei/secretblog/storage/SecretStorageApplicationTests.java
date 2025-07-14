package com.shijiawei.secretblog.storage;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
class SecretStorageApplicationTests {


    @Test
    void contextLoads() {
        String s = "abcd.jpg";
        String[] split = s.split("\\.");
        System.out.println(split[1]);

    }

    @Test
    public void test() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        MinioClient minioClient = MinioClient.builder()
                .endpoint("https://static-host-ah8wqcve-secret.ap-northeast-1.clawcloudrun.com")
                .credentials("ah8wqcve", "ms4xccbfqjzsk8zz")
                .build();
//        minioClient = MinioClient.builder()
//                .endpoint("http://4.240.82.138:9001")
//                .credentials("p6KMsat29WtFgrWPasyR", "bNgTw96toBr5LbGrzKFnL4oXIV0g7khBYuPB4Zhs")
//                .build();

        String storageName= UUID.randomUUID().toString();
        String presignedObjectUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket("secretblog")
                .object(storageName)
                .method(Method.POST)

                .expiry(30, TimeUnit.MINUTES)
                .build());

        if(StringUtils.isNotEmpty(presignedObjectUrl)){
            System.out.println("成功生成預簽名 URL:"+presignedObjectUrl);
        }
    }
}
