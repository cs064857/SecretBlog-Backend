package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.utils.JwtService;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * ClassName: AmsCommentServiceImpl
 * Description:
 *
 * @Create 2025/7/16 上午3:02
 */
@Slf4j
@Service
public class AmsCommentServiceImpl extends ServiceImpl<AmsCommentMapper, AmsComment> implements AmsCommentService {

    @Autowired
    private JwtService jwtService; // 注入 JwtService 依賴

    @Override
    public R createComment(AmsCommentCreateDTO amsCommentCreateDTO) {
        String jwtToken = amsCommentCreateDTO.getJwtToken();

        String userIdFromToken = null;
        try {
            // 驗證並解析 JWT Token
            Map<String, Object> hashMap = jwtService.verifyJwt(jwtToken, HashMap.class);
            if (hashMap == null) {
                log.error("JWT Token 驗證失敗或已過期");
                return R.error("Token 無效或已過期");
            }

            // 從 Token 中獲取用戶ID
            userIdFromToken = (String) hashMap.get("userId");
            if (userIdFromToken == null) {
                log.error("Token 中未找到 userId 信息");
                return R.error("Token 中缺少用戶信息");
            }

            Long userId = Long.parseLong(userIdFromToken);
            log.debug("從Token解析的userId: {}", userId);

            // 使用從 Token 解析的 userId，而不是 DTO 中的 userId
            // 這樣可以確保安全性，防止用戶偽造 userId

            return Optional.ofNullable(amsCommentCreateDTO.getArticleId())
                    .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
                    .filter(cmt -> !cmt.trim().isEmpty())
                    .map(cmt -> {
                        AmsComment amsComment = new AmsComment();
                        amsComment.setUserId(userId); // 使用從 Token 解析的 userId
                        amsComment.setArticleId(amsCommentCreateDTO.getArticleId());
                        amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());
                        amsComment.setCreateAt(LocalDateTime.now());
                        amsComment.setUpdateAt(LocalDateTime.now());
                        this.baseMapper.insert(amsComment);
                        log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
                        return R.ok("評論發布成功");
                    })
                    .orElseGet(() -> {
                        log.error("創建評論失敗：評論內容為空或文章ID無效");
                        return R.error("評論內容不能為空");
                    });

        } catch (NumberFormatException e) {
            log.error("Token 中的 userId 格式錯誤: {}", userIdFromToken, e);
            return R.error("用戶信息格式錯誤");
        } catch (Exception e) {
            log.error("JWT Token 解析失敗", e);
            return R.error("Token 驗證失敗，請重新登錄");
        }
    }
}
