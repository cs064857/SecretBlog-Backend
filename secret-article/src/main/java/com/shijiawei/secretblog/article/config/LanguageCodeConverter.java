package com.shijiawei.secretblog.article.config;

import com.shijiawei.secretblog.article.myenum.LanguageCode;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * 將查詢參數中的語言代碼轉為LanguageCode枚舉。
 */
@Component
public class LanguageCodeConverter implements Converter<String, LanguageCode> {

    @Override
    public LanguageCode convert(String source) {
        return LanguageCode.fromCode(source);
    }
}

