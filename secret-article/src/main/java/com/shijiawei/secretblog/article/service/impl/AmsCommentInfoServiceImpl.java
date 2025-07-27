package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsCommentInfo;
import com.shijiawei.secretblog.article.mapper.AmsCommentInfoMapper;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.service.AmsCommentInfoService;
import org.springframework.stereotype.Service;

/**
 * ClassName: AmsCommentInfoService
 * Description:
 *
 * @Create 2025/7/22 上午4:01
 */
@Service
public class AmsCommentInfoServiceImpl extends ServiceImpl<AmsCommentInfoMapper, AmsCommentInfo> implements AmsCommentInfoService {
}
