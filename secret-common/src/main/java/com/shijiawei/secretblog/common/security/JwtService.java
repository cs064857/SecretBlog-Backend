package com.shijiawei.secretblog.common.security;


import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;

@Service
public class JwtService implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private PrivateKey privateKey;

    private JwtParser jwtParser;

    public String createJwt(Object jwtPayload, long expiredAt) {
        //添加構成JWT的參數
        Map<String, Object> headMap = new HashMap();
        headMap.put("alg", SignatureAlgorithm.RS256.getValue());//使用RS256簽名算法
        headMap.put("typ", "JWT");
        Map body = JSON.parse(JSON.stringify(jwtPayload), HashMap.class);
        String jwt = Jwts.builder()
                .setHeader(headMap)
                .setClaims(body)
                .setExpiration(new Date(expiredAt))
                .signWith(privateKey)
                .compact();
        return jwt;
    }

    @Value("${login.jwt.private-key}")
    private String privateKeyBase64;
    //獲取私鑰，用於生成Jwt
    private PrivateKey getPrivateKey() {
        try {
            logger.debug("開始初始化私鑰，私鑰長度: {}", privateKeyBase64 != null ? privateKeyBase64.length() : "null");
            // 利用JDK自帶的工具生成私鑰
            KeyFactory kf = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(Decoders.BASE64.decode(privateKeyBase64));
            return kf.generatePrivate(ks);
        } catch (Exception e) {
      logger.error("獲取Jwt私鑰失敗，詳細錯誤: {}", e.getMessage(), e);
//      logger.error("私鑰內容: {}", privateKeyBase64);

            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                    .cause(e.getCause())
                    .detailMessage("獲取Jwt私鑰失敗, 格式錯誤或配置為空")
                    .data(Map.of("keyLength",privateKeyBase64!=null ? privateKeyBase64.length() : 0))
                    .build();
        }
    }

    @Value("${login.jwt.public-key}")
    private String publicKeyBase64;
    // 公鑰，用於解析Jwt
    private JwtParser getJwtParser() {
        try {
            logger.debug("開始初始化 JWT Parser，公鑰長度: {}", publicKeyBase64 != null ? publicKeyBase64.length() : "null");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Decoders.BASE64.decode(publicKeyBase64));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pk = keyFactory.generatePublic(keySpec);
            return Jwts.parserBuilder().setSigningKey(pk).build();
        } catch (Exception e) {
            // 獲取公鑰失敗 - 記錄詳細錯誤信息
//      logger.error("獲取Jwt公鑰失敗，詳細錯誤: {}", e.getMessage(), e);
//      logger.error("公鑰內容: {}", publicKeyBase64);
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.JWT_CONFIG_ERROR)
                    .cause(e.getCause())
                    .detailMessage("獲取Jwt公鑰失敗, 格式錯誤或配置為空")
                    .data(Map.of("keyLength",publicKeyBase64!=null ? publicKeyBase64.length() : 0,
                            "publicKeyBase64", StringUtils.abbreviate(StringUtils.defaultString(publicKeyBase64), 20)))
                    .build();
//      throw new CustomRuntimeException(ResultCode.JWT_CONFIG_ERROR.getCode(),ResultCode.JWT_CONFIG_ERROR.getMessage(), e.getCause());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        privateKey = getPrivateKey();
        jwtParser = getJwtParser();
    }

    public <T> T verifyJwt(String jwt, Class<T> jwtPayloadClass) {
        if (jwt == null || jwt.equals("")) {
            return null;
        }
        Jws<Claims> jws = this.jwtParser.parseClaimsJws(jwt); // 會校驗簽名，校驗過期時間
        Claims jwtPayload = jws.getBody();
        if (jwtPayload == null) {
            return null;
        }
        return JSON.convert(jwtPayload, jwtPayloadClass);
    }

    public static <T> T getPayload(String jwt, Class<T> jwtPayloadClass) {
        if (jwt == null || jwt.equals("")) {
            return null;
        }

        try {
            // jwt字符串由3部分組成，用英文的點分割：herder.payload.sign
            // 可以直接取中間一段，進行Base64解碼
            byte[] decodedBytes = Base64.getDecoder().decode(jwt.split("\\.")[1]);
            return JSON.parse(new String(decodedBytes), jwtPayloadClass);
        } catch (Exception e) {
            return null;
        }
    }
}
