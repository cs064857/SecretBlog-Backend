package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.entity.AmsCommentInfo;
import com.shijiawei.secretblog.article.feign.UserFeignClient;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.service.AmsCommentInfoService;
import com.shijiawei.secretblog.article.service.AmsCommentService;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.utils.JwtService;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.TimeTool;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Autowired
    private AmsCommentInfoService amsCommentInfoService;

    private final UserFeignClient userFeignClient;

    public AmsCommentServiceImpl(UserFeignClient userFeignClient) {
        this.userFeignClient = userFeignClient;
    }




//    @Transactional
//    @Override
//    public R createComment(AmsCommentCreateDTO amsCommentCreateDTO) {
//
//        String jwtToken = amsCommentCreateDTO.getJwtToken();
//
//        String userIdFromToken = null;
//        try {
//            // 驗證並解析 JWT Token
//            Map<String, Object> hashMap = jwtService.verifyJwt(jwtToken, HashMap.class);
//            if (hashMap == null) {
//                log.error("JWT Token 驗證失敗或已過期");
//                return R.error("Token 無效或已過期");
//            }
//
//            // 從 Token 中獲取用戶ID
//            userIdFromToken = (String) hashMap.get("userId");
//            if (userIdFromToken == null) {
//                log.error("Token 中未找到 userId 信息");
//                return R.error("Token 中缺少用戶信息");
//            }
//
//            // 使用從 Token 解析的 userId
//            Long userId = Long.parseLong(userIdFromToken);
//            log.debug("從Token解析的userId: {}", userId);
//
//
//            if(amsCommentCreateDTO.getParent_comment_id()==null){
//                return Optional.ofNullable(amsCommentCreateDTO.getArticleId())
//                        .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
//                        .filter(cmt -> !cmt.trim().isEmpty())
//                        .map(cmt -> {
//                            AmsComment amsComment = new AmsComment();
//                            AmsCommentInfo amsCommentInfo = new AmsCommentInfo();
//                            amsCommentInfo.setUserId(userId); // 使用從 Token 解析的 userId
//                            amsCommentInfo.setArticleId(amsCommentCreateDTO.getArticleId());
//                            amsCommentInfo.setCreateAt(LocalDateTime.now());
//
//
//                            long commentId = IdWorker.getId(amsComment);
//                            long commentInfoId = IdWorker.getId(amsCommentInfo);
//                            amsComment.setId(commentId);
//                            amsComment.setCommentInfoId(commentInfoId);
//
//                            amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());
//
//                            this.baseMapper.insert(amsComment);
//                            amsCommentInfo.setId(commentInfoId);
//                            amsCommentInfo.setCommentId(commentId);
//                            amsCommentInfo.setUpdateAt(LocalDateTime.now());
//
//                            amsCommentInfoService.save(amsCommentInfo);
//
//
//
//                            log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
//                            return R.ok("評論發布成功");
//                        })
//                        .orElseGet(() -> {
//                            log.error("創建評論失敗：評論內容為空或文章ID無效");
//                            return R.error("評論內容不能為空");
//                        });
//            }else {
//                return Optional.ofNullable(amsCommentCreateDTO.getArticleId())
//                        .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
//                        .filter(cmt -> !cmt.trim().isEmpty())
//                        .map(cmt -> {
//                            AmsComment amsComment = new AmsComment();
//                            AmsCommentInfo amsCommentInfo = new AmsCommentInfo();
//                            amsCommentInfo.setUserId(userId); // 使用從 Token 解析的 userId
//                            amsCommentInfo.setArticleId(amsCommentCreateDTO.getArticleId());
//                            amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());
//                            amsCommentInfo.setCreateAt(LocalDateTime.now());
//                            amsCommentInfo.setUpdateAt(LocalDateTime.now());
//                            amsCommentInfo.setParentCommentId(amsCommentCreateDTO.getParent_comment_id());
//                            //透過ParentCommentId去查詢到該父評論,並將其留言數量+1
//                            AmsCommentInfo parentAmsCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getId, amsCommentCreateDTO.getParent_comment_id()));
//                            parentAmsCommentInfo.setReplysCount(parentAmsCommentInfo.getReplysCount()+1);
//
//
//                            this.baseMapper.insert(amsComment);
//                            //儲存新創建的評論
//                            amsCommentInfoService.save(amsCommentInfo);
//                            //更新父評論
//                            amsCommentInfoService.updateById(parentAmsCommentInfo);
//                            log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
//                            return R.ok("評論發布成功");
//                        })
//                        .orElseGet(() -> {
//                            log.error("創建評論失敗：評論內容為空或文章ID無效");
//                            return R.error("評論內容不能為空");
//                        });
//            }
//
//
//        } catch (NumberFormatException e) {
//            log.error("Token 中的 userId 格式錯誤: {}", userIdFromToken, e);
//            return R.error("用戶信息格式錯誤");
//        } catch (Exception e) {
//            log.error("JWT Token 解析失敗", e);
//            return R.error("Token 驗證失敗，請重新登錄");
//        }
//    }

//    @PostMapping("/logout")
//    public R logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//
//        String originalJwtToken = null;
//        // 1) 優先：從認證物件(MyJwtAuthentication)取出在過濾器中保存的原始JWT
//        if (authentication instanceof com.shijiawei.secretblog.user.authentication.handler.login.business.MyJwtAuthentication auth) {
//            originalJwtToken = auth.getJwtToken();
//        }
//
//        log.info("Logout originalJwtToken: {}", originalJwtToken);
//
//        // 將當前 sessionId 放入黑名單（使用剩餘有效期作為TTL）
//        if (authentication != null && authentication.isAuthenticated()) {
//            Object principal = authentication.getPrincipal();
//            if (principal instanceof UserLoginInfo currentUser) {
//                long now = TimeTool.nowMilli();
//                long expiredTime = currentUser.getExpiredTime();
//                long ttl = Math.max(expiredTime - now, 1000L); // 至少1秒，避免0或負數
//                try {
//                    tokenBlacklistService.blacklist(currentUser.getSessionId(), ttl);
//                    log.info("SessionId {} 已加入黑名單, TTL={}ms", currentUser.getSessionId(), ttl);
//                } catch (Exception e) {
//                    log.warn("加入黑名單失敗: {}", e.getMessage(), e);
//                }
//            }
//        }
//
//        // 清除安全上下文
//        SecurityContextHolder.clearContext();
//        return R.ok("登出成功");
//    }


    @Transactional
    @Override
    public R createComment(AmsCommentCreateDTO amsCommentCreateDTO) {

//        String jwtToken = amsCommentCreateDTO.getJwtToken();

        String userIdFromToken = null;
        try {
            // 驗證並解析 JWT Token
//            Map<String, Object> hashMap = jwtService.verifyJwt(jwtToken, HashMap.class);
//            if (hashMap == null) {
//                log.error("JWT Token 驗證失敗或已過期");
//                return R.error("Token 無效或已過期");
//            }
//
//            // 從 Token 中獲取用戶ID
//            userIdFromToken = (String) hashMap.get("userId");
//            if (userIdFromToken == null) {
//                log.error("Token 中未找到 userId 信息");
//                return R.error("Token 中缺少用戶信息");
//            }
//
//            // 使用從 Token 解析的 userId
//            Long userId = Long.parseLong(userIdFromToken);
//            log.debug("從Token解析的userId: {}", userId);


            if(!UserContextHolder.isCurrentUserLoggedIn()){
                log.warn("未取得登入用戶信息，拒絕新增評論");
                throw new IllegalStateException("用戶未登入，無法新增評論");
            }
            Long userId = UserContextHolder.getCurrentUserId();


            if(amsCommentCreateDTO.getParent_comment_id()==null){
                return Optional.ofNullable(amsCommentCreateDTO.getArticleId())
                        .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
                        .filter(cmt -> !cmt.trim().isEmpty())
                        .map(cmt -> {
                            AmsComment amsComment = new AmsComment();
                            AmsCommentInfo amsCommentInfo = new AmsCommentInfo();
                            amsCommentInfo.setUserId(userId); // 使用從 Token 解析的 userId
                            amsCommentInfo.setArticleId(amsCommentCreateDTO.getArticleId());
                            amsCommentInfo.setCreateAt(LocalDateTime.now());


                            long commentId = IdWorker.getId(amsComment);
                            long commentInfoId = IdWorker.getId(amsCommentInfo);
                            amsComment.setId(commentId);
                            amsComment.setCommentInfoId(commentInfoId);

                            amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());

                            this.baseMapper.insert(amsComment);
                            amsCommentInfo.setId(commentInfoId);
                            amsCommentInfo.setCommentId(commentId);
                            amsCommentInfo.setUpdateAt(LocalDateTime.now());

                            amsCommentInfoService.save(amsCommentInfo);



                            log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
                            return R.ok("評論發布成功");
                        })
                        .orElseGet(() -> {
                            log.error("創建評論失敗：評論內容為空或文章ID無效");
                            return R.error("評論內容不能為空");
                        });
            }else {
                return Optional.ofNullable(amsCommentCreateDTO.getArticleId())
                        .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
                        .filter(cmt -> !cmt.trim().isEmpty())
                        .map(cmt -> {
                            AmsComment amsComment = new AmsComment();
                            AmsCommentInfo amsCommentInfo = new AmsCommentInfo();
                            amsCommentInfo.setUserId(userId); // 使用從 Token 解析的 userId
                            amsCommentInfo.setArticleId(amsCommentCreateDTO.getArticleId());
                            amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());
                            amsCommentInfo.setCreateAt(LocalDateTime.now());
                            amsCommentInfo.setUpdateAt(LocalDateTime.now());
                            amsCommentInfo.setParentCommentId(amsCommentCreateDTO.getParent_comment_id());
                            //透過ParentCommentId去查詢到該父評論,並將其留言數量+1
                            AmsCommentInfo parentAmsCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getId, amsCommentCreateDTO.getParent_comment_id()));
                            parentAmsCommentInfo.setReplysCount(parentAmsCommentInfo.getReplysCount()+1);


                            this.baseMapper.insert(amsComment);
                            //儲存新創建的評論
                            amsCommentInfoService.save(amsCommentInfo);
                            //更新父評論
                            amsCommentInfoService.updateById(parentAmsCommentInfo);
                            log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
                            return R.ok("評論發布成功");
                        })
                        .orElseGet(() -> {
                            log.error("創建評論失敗：評論內容為空或文章ID無效");
                            return R.error("評論內容不能為空");
                        });
            }


        } catch (NumberFormatException e) {
            log.error("Token 中的 userId 格式錯誤: {}", userIdFromToken, e);
            return R.error("用戶信息格式錯誤");
        } catch (Exception e) {
            log.error("JWT Token 解析失敗", e);
            return R.error("Token 驗證失敗，請重新登錄");
        }
    }

    @Override
    public List<AmsArtCommentsVo> getArtComments(Long articleId) {
        //根據該文章的ArticleId查找所有關聯的CommentInfo
        List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
        if(amsCommentInfoList.isEmpty()){
            return null;
        }
        List<Long> amsCommentIds = amsCommentInfoList.stream()
                .map(AmsCommentInfo::getCommentId)
                .collect(Collectors.toList());


        log.info("所有留言的Ids:{}",amsCommentIds);
        //根據CommentInfo中的CommentId查找該文章所有的留言
        List<AmsComment> amsCommentList = this.baseMapper.selectList(new LambdaQueryWrapper<AmsComment>().in(AmsComment::getId, amsCommentIds));
        log.info("所有留言:{}",amsCommentList);
        //將commentList轉為MAP屬性,KEY為ID、VALUE為對象,方便與CommentInfo的內容對應上
        Map<Long, AmsComment> amsCommentCollect = amsCommentList.stream().collect(Collectors.toMap(AmsComment::getId, Function.identity()));
        Map<Long, AmsCommentInfo> amsCommentInfoCollect = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getId, Function.identity()));
//        List<AmsArtCommentsVo> amsArtCommentsFromParentCommentId = getAmsArtCommentsFromParentCommentId(amsCommentInfo.getParentCommentId());
        //該文章下所有是父留言的留言ID
//        List<Long> amsParentCommentIds = amsCommentInfoList.stream()
//                .map(AmsCommentInfo::getParentCommentId)
//                .collect(Collectors.toList());

        //TODO 利用用戶的ID來獲取用戶名稱
        List<Long> userIds = amsCommentInfoList.stream().map(AmsCommentInfo::getUserId).toList();
        R<List<UserBasicDTO>> usersByIds = userFeignClient.selectUserBasicInfoByIds(userIds);

        Map<Long,UserBasicDTO> userBasicDTOLongMap = Optional.ofNullable(usersByIds)
                .map(R::getData)//若usersByIds不為空,則取出Data
                .orElse(Collections.emptyList())//若Data為空,則給一個空的List
                .stream()
                .collect(Collectors.toMap(UserBasicDTO::getUserId, Function.identity()));//將List轉為Map,KEY為UserBasicDTO對象、VALUE為UserId


        List<AmsArtCommentsVo> amsArtCommentsVos = amsCommentInfoList.stream().map(amsCommentInfo -> {
            AmsArtCommentsVo amsArtCommentsVo = new AmsArtCommentsVo();
            AmsComment amsComment = amsCommentCollect.get(amsCommentInfo.getCommentId());
            amsArtCommentsVo.setCommentContent(amsComment.getCommentContent());


//            if(!userBasicDTOLongMap.isEmpty())

            if(!userBasicDTOLongMap.isEmpty()){
                UserBasicDTO userBasicDTO = userBasicDTOLongMap.get(amsCommentInfo.getUserId());
                amsArtCommentsVo.setUsername(userBasicDTO.getNickName());
                amsArtCommentsVo.setAvatar(userBasicDTO.getAvatar());
            }


            amsArtCommentsVo.setLikesCount(amsCommentInfo.getLikesCount());
            amsArtCommentsVo.setReplysCount(amsCommentInfo.getReplysCount());
            amsArtCommentsVo.setCreateAt(amsCommentInfo.getCreateAt());
            amsArtCommentsVo.setUpdateAt(amsCommentInfo.getUpdateAt());

            amsArtCommentsVo.setCommentId(amsCommentInfo.getCommentId());
            if (amsCommentInfo.getParentCommentId() != null) {
                //TODO 查詢父留言中的資料
                //拿parentCommentIds去這篇文章中的所有留言來搜尋到父留言

                AmsComment parentComment = amsCommentCollect.get(amsCommentInfo.getParentCommentId());
                AmsCommentInfo parentCommentInfo = amsCommentInfoCollect.get(parentComment.getCommentInfoId());

                log.info("留言ID:{},父留言ID:{}", amsCommentInfo.getCommentId(), parentComment.getId());
                log.info("留言ID:{},父留言InfoID:{}", amsCommentInfo.getCommentId(), parentCommentInfo.getId());
                log.info("留言ID:{},父留言對象:{}", amsCommentInfo.getCommentId(), parentComment);
                log.info("留言ID:{},父留言Info對象:{}", amsCommentInfo.getCommentId(), parentCommentInfo);
                //包裝父留言
                AmsArtCommentsVo artParentCommentsVo = new AmsArtCommentsVo();
                BeanUtils.copyProperties(parentComment,artParentCommentsVo);
                BeanUtils.copyProperties(parentCommentInfo,artParentCommentsVo);
                amsArtCommentsVo.setParentComment(artParentCommentsVo);

                amsArtCommentsVo.setUsername("test");
                log.info("留言ID:{},父留言包裝後Vo對象:{}", amsCommentInfo.getCommentId(), artParentCommentsVo);
                return amsArtCommentsVo;
            }
            return amsArtCommentsVo;
        }).collect(Collectors.toList());

        log.info("amsArtCommentsVos:{}",amsArtCommentsVos);
        return amsArtCommentsVos;
    }
}
