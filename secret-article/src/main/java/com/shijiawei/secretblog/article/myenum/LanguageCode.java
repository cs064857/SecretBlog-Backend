package com.shijiawei.secretblog.article.myenum;

import lombok.Getter;

import java.util.Arrays;

/**
 * ClassName: LanguageCode
 * Description:
 *
 * @Create 2026/3/12 下午7:33
 */
@Getter
public enum LanguageCode {

    ENGLISH("en","English"),
    JAPANESE("ja","JAPANESE"),
    KOREAN("ko","Korean");


    private final String langCode;
    private final String targetLangStr;

    LanguageCode(String langCode, String targetLangStr) {
        this.langCode = langCode;
        this.targetLangStr = targetLangStr;
    }

    public static LanguageCode fromCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("languageCode不可為空");
        }

        for (LanguageCode value : values()) {
            if (value.getLangCode().equalsIgnoreCase(code.trim())) {
                return value;
            }
        }

        throw new IllegalArgumentException("不支援的languageCode:" + code);
    }
}
