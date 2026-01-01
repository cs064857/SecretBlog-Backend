package com.shijiawei.secretblog.user;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.util.EncodingUtils;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class SecretUserApplicationTests {

    @Test
    void contextLoads() {
        System.out.println("測試");
    }

    @Test
    public void testPasswordBCrtpt() {
        String password = "testPassword";

        // 使用BCrypt生成不可逆的雜湊值
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String encode = bCryptPasswordEncoder.encode(password);
        log.info("加密過後密碼:{}", encode); // 例如$2a$10$GcU00vz3BNauywcvey8rtuGnuSQ3GhqRijnoMnpDGOFG.0qH4CiPC



        // 檢查現有雜湊值是否需要升級（例如提升 cost factor）
//        boolean upgradedEncoding = bCryptPasswordEncoder.upgradeEncoding(encode);
//        if(upgradedEncoding) {
//            encode = bCryptPasswordEncoder.encode(password);//重新加密
//        }

        // 比較明文密碼與已雜湊的密碼是否匹配
        boolean matches = bCryptPasswordEncoder.matches("testPassword", encode);//true
        boolean matches2 = bCryptPasswordEncoder.matches("嗨嗨", encode);//false
        log.info("matches:{}", matches);
        log.info("matches2:{}", matches);
    }
    @Test
    public void testArgon2(){

    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testPasswordEncoderBean() {
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
        String raw = "Password20250705";
        String encoded = passwordEncoder.encode(raw);
        log.info("Encoded password for verification: {}", encoded);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
    }
}
