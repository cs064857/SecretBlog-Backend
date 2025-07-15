package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.R;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * ClassName: AmsCommentServiceImpl
 * Description:
 *
 * @Create 2025/7/16 上午3:02
 */
@Service
public class AmsCommentServiceImpl extends ServiceImpl<AmsCommentMapper, AmsComment> implements AmsCommentService {


    @Override
    public R createComment(AmsCommentCreateDTO amsCommentCreateDTO) {




        return Optional.ofNullable(amsCommentCreateDTO.getUserId())
                .flatMap(uId->Optional.ofNullable(amsCommentCreateDTO.getArticleId()))
                    .flatMap(artId->Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
                .filter(cmt->!cmt.trim().isEmpty())
                .map(cmt->{

                    AmsComment amsComment = new AmsComment();
                    amsComment.setUserId(amsCommentCreateDTO.getUserId());
                    amsComment.setArticleId(amsCommentCreateDTO.getArticleId());
                    amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());
                    amsComment.setCreateAt(LocalDateTime.now());
                    amsComment.setUpdateAt(LocalDateTime.now());
                    this.baseMapper.insert(amsComment);
                    return R.ok();
                })
                .orElseGet(()->{
                            log.error("創建評論失敗");
                            return R.error();
                        }

                );



    }
}
