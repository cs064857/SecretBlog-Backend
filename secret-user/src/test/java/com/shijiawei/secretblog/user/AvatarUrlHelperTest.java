package com.shijiawei.secretblog.user;

import com.shijiawei.secretblog.common.utils.AvatarUrlHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
public class AvatarUrlHelperTest {

    private static final String MINIO_DOMAIN = "https://test.storage";

    @Test
    public void testToStoragePath() {
        String avatar = "https://test.storage/user-assets-pub/e260073d-0f70-4754-8d7a-2da596ca861a.png";
        String storagePath = AvatarUrlHelper.toStoragePath(avatar, MINIO_DOMAIN);
        System.out.println("testToStoragePath - storagePath:"+storagePath);//user-assets-pub/e260073d-0f70-4754-8d7a-2da596ca861a.png

    }

    @Test
    public void TestToPublicUrl() {
        String path = "user-assets-pub/default.svg";
        String publicUrl = AvatarUrlHelper.toPublicUrl(path, MINIO_DOMAIN);
        System.out.println("TestToPublicUrl - publicUrl:"+publicUrl);//https://test.storage/user-assets-pub/default.svg

    }
}
