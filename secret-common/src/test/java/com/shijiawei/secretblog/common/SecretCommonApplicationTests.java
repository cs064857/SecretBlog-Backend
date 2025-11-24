package com.shijiawei.secretblog.common;

import com.shijiawei.secretblog.common.utils.R;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Timestamp;
import java.time.Instant;

//@SpringBootTest
class SecretCommonApplicationTests {

	@Test
	void contextLoads() {
	}


    @Test
    void testR() {
        Timestamp timestamp = Timestamp.from(Instant.now());
        R<Void> r = R.error("1001", "系統異常，請稍後再試", timestamp);
        System.out.println(r);
    }
}
