package com.shijiawei.secretblog.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.article.myenum.LanguageCode;
import com.shijiawei.secretblog.common.utils.R;

/**
 * ClassName: AmsAiService
 * Description:
 *
 * @Create 2026/3/12 下午7:24
 */
public interface AmsAiService {
    String translateArticleContext(Long articleId , LanguageCode languageCode);

}
