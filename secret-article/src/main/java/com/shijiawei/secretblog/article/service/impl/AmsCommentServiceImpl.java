package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.entity.AmsArtStatus;
import com.shijiawei.secretblog.article.entity.AmsArticle;
import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.entity.AmsCommentAction;
import com.shijiawei.secretblog.article.entity.AmsCommentInfo;
import com.shijiawei.secretblog.article.entity.AmsCommentStatistics;
import com.shijiawei.secretblog.article.feign.UserFeignClient;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.service.*;

import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.vo.AmsUserCommentVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.article.dto.AmsCommentEditDTO;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.exception.BusinessException;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.message.ArticleRepliedEmailNotifyMessage;
import com.shijiawei.secretblog.common.message.CommentRepliedEmailNotifyMessage;
import com.shijiawei.secretblog.common.message.RabbitMessageProducer;
import com.shijiawei.secretblog.common.message.UpdateCommentActionMessage;
import com.shijiawei.secretblog.common.message.UpdateCommentLikedMessage;
import com.shijiawei.secretblog.common.myenum.RedisBloomFilterKey;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.myenum.RedisLockKey;
import com.shijiawei.secretblog.common.myenum.RedisOpenCacheKey;
import com.shijiawei.secretblog.common.redisutils.RedisLuaScripts;
import com.shijiawei.secretblog.common.security.JwtService;
import com.shijiawei.secretblog.common.utils.*;
import com.shijiawei.secretblog.common.utils.redis.RedisBloomFilterUtils;
import com.shijiawei.secretblog.common.utils.redis.RedisCacheLoaderUtils;
import com.shijiawei.secretblog.common.utils.redis.RedisIncrementUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.*;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

    private final RedissonClient redissonClient;

    private final UserFeignClient userFeignClient;

    @Autowired
    private RedisBloomFilterUtils redisBloomFilterUtils;

    @Autowired
    private AmsArticleService amsArticleService;

    @Autowired
    @Lazy
    private AmsCommentService self;

    @Autowired
    private RedisIncrementUtils redisIncrementUtils;
    @Autowired
    private AmsArtinfoService amsArtinfoService;
    @Autowired
    private AmsArtStatusService amsArtStatusService;

    @Autowired
    private RedisCacheLoaderUtils redisCacheLoaderUtils;

    @Autowired
    private AmsCommentStatisticsService amsCommentStatisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AmsCommentActionService amsCommentActionService;

    @Autowired
    private RabbitMessageProducer rabbitMessageProducer;

    @Value("${comment.edit-window-minutes:15}")
    private Integer editWindowMinutes;

//    public AmsCommentServiceImpl(RedissonClient redissonClient){
//        this.redissonClient = redissonClient;
//    }
//
//
//    public AmsCommentServiceImpl(UserFeignClient userFeignClient) {
//        this.userFeignClient = userFeignClient;
//    }

    public AmsCommentServiceImpl(UserFeignClient userFeignClient,RedissonClient redissonClient) {
        this.userFeignClient = userFeignClient;
        this.redissonClient = redissonClient;
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
//                return R.error("Token 中缺少用戶信息");
//            }
//
//            // 使用從 Token 解析的 userId
//            Long userId = Long.parseLong(userIdFromToken);
//            log.debug("從Token解析的userId: {}", userId);
//
//
//            if(amsCommentCreateDTO.getParentCommentId()==null){
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
//                            log.info("留言創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
//                            return R.ok("留言發布成功");
//                        })
//                        .orElseGet(() -> {
//                            log.error("創建留言失敗：留言內容為空或文章ID無效");
//                            return R.error("留言內容不能為空");
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
//                            amsCommentInfo.setParentCommentId(amsCommentCreateDTO.getParentCommentId());
//                            //透過ParentCommentId去查詢到該父留言,並將其留言數量+1
//                            AmsCommentInfo parentAmsCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getId, amsCommentCreateDTO.getParentCommentId()));
//                            parentAmsCommentInfo.setrepliesCount(parentAmsCommentInfo.getrepliesCount()+1);
//
//
//                            this.baseMapper.insert(amsComment);
//                            //儲存新創建的留言
//                            amsCommentInfoService.save(amsCommentInfo);
//                            //更新父留言
//                            amsCommentInfoService.updateById(parentAmsCommentInfo);
//                            log.info("留言創建成功，用戶ID: {}, 文章ID: {}", userId, amsCommentCreateDTO.getArticleId());
//                            return R.ok("留言發布成功");
//                        })
//                        .orElseGet(() -> {
//                            log.error("創建留言失敗：留言內容為空或文章ID無效");
//                            return R.error("留言內容不能為空");
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

    /**
     * 創建文章留言
     * @param articleId 文章ID
     * @param amsCommentCreateDTO 留言內容DTO
     * @return 留言創建結果
     */
    @Transactional(rollbackFor = Exception.class)
    @DelayDoubleDelete(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX,key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY)
    @Override
    public R createComment(Long articleId,AmsCommentCreateDTO amsCommentCreateDTO) {

//        String articleBloomFilterKey = RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey();
//        RBloomFilter<Long> articleBloomFilter = redissonClient.getBloomFilter(articleBloomFilterKey);
//        if(!articleBloomFilter.contains(articleId)){
//            log.warn("文章不存在，articleId={}",articleId);
//            throw new CustomRuntimeException("文章不存在");
//        }

        redisBloomFilterUtils.requireExists(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey(),articleId,"文章不存在");

//        log.info("回覆留言 amsCommentCreateDTO:{}",amsCommentCreateDTO);
//        String jwtToken = amsCommentCreateDTO.getJwtToken();

        if(amsCommentCreateDTO.getCommentContent().isBlank()){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("新增留言時留言內容不可為空")
                    .build();
        }

        String userIdFromToken = null;

        try {

            if (!UserContextHolder.isCurrentUserLoggedIn()) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.UNAUTHORIZED)
                        .detailMessage("用戶未登入，無法新增留言")
                        .build();
            }
            Long userId = UserContextHolder.getCurrentUserId();

            R<UserBasicDTO> user = userFeignClient.getUserById(userId);


            long commentId = IdWorker.getId();
            long commentInfoId = IdWorker.getId();

            AmsCommentInfo amsCommentInfo = AmsCommentInfo.builder()
                    .userId(userId)
                    .articleId(articleId)
                    .id(commentInfoId)
                    .commentId(commentId)
                    .nickName(user.getData().getNickName())
                    .avatar(user.getData().getAvatar())
                    .build();


            AmsComment amsComment = AmsComment.builder()
                    .id(commentId)
                    .commentInfoId(commentInfoId)
                    .commentContent(amsCommentCreateDTO.getCommentContent())
                    .build();

            this.baseMapper.insert(amsComment);

            if (amsCommentCreateDTO.getParentCommentId() != null) {
                amsCommentInfo.setParentCommentId(amsCommentCreateDTO.getParentCommentId());
                //透過ParentCommentId去查詢到該父留言的指標,並將其留言數量+1
                boolean update = amsCommentStatisticsService.update(new LambdaUpdateWrapper<AmsCommentStatistics>()
                        .eq(AmsCommentStatistics::getCommentId, amsCommentCreateDTO.getParentCommentId())
                        .setSql("replies_count = replies_count + 1")
                );
                if(!update){
                    throw BusinessRuntimeException.builder()
                            .iErrorCode(ResultCode.CREATE_FAILED)
                            .detailMessage("創建留言時對父留言的留言數指標遞增時出現錯誤")
                            .data(Map.of(
                                            "articleId",articleId,
                                            "parentCommentId",amsCommentCreateDTO.getParentCommentId(),
                                            "commentId",commentId
                            ))
                            .build();

                }
            }
            amsCommentInfoService.save(amsCommentInfo);

            //初始化留言指標
            AmsCommentStatistics amsCommentStatistics = AmsCommentStatistics.builder()
                    .articleId(articleId)
                    .commentId(commentId)
                    .likesCount(0)
                    .repliesCount(0)
                    .build();

            amsCommentStatisticsService.save(amsCommentStatistics);

            //初始化留言互動記錄
            AmsCommentAction amsCommentAction = AmsCommentAction.builder()
                    .commentId(commentId)
                    .articleId(articleId)
                    .userId(userId)
                    .isLiked((byte) 0)
                    .isBookmarked((byte) 0)
                    .build();
            amsCommentActionService.save(amsCommentAction);

            //將資料庫中的文章留言數增加, 異步雙寫(DB為主、Redis為輔(可能存在與DB不一致))

            boolean amsStatusIsUpdate = amsArtStatusService.update(
                    new LambdaUpdateWrapper<AmsArtStatus>()
                            .eq(AmsArtStatus::getArticleId, articleId)
                            .setSql("comments_count = comments_count + 1")
            );
            if(!amsStatusIsUpdate){
//                                throw new CustomRuntimeException("更新文章留言數失敗");
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.UPDATE_FAILED)
                        .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId,"")))
                        .build();
            }
            log.info("文章留言數更新成功，文章ID: {}", articleId);




            final String commentLikesKey= String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern(),articleId);
            final String commentRepliesKey= String.format(RedisCacheKey.ARTICLE_COMMENT_REPLIES_COUNT_HASH.getPattern(),articleId);
            Map<Long, Integer> initStatistics = Map.of(commentId, 0);

            redisBloomFilterUtils.saveMapToRMapAfterCommit(commentLikesKey,initStatistics, Long.class, Integer.class);
            redisBloomFilterUtils.saveMapToRMapAfterCommit(commentRepliesKey,initStatistics, Long.class, Integer.class);


            // 新增：在事務提交後將 commentInfoId 放入布隆過濾器
            redisBloomFilterUtils.saveToBloomFilterAfterCommit(
                    commentId,
                    RedisBloomFilterKey.COMMENT_BLOOM_FILTER.getKey()
            );
            //在事務提交後將 將Redis快取中的文章留言數量++
            redisIncrementUtils.afterCommitIncrement(RedisCacheKey.ARTICLE_COMMENTS.format(articleId));

            // 回覆 Email 通知，實際寄信
            sendReplyEmailNotifyAfterCommit(
                    articleId,
                    commentId,
                    amsCommentCreateDTO.getParentCommentId(),
                    userId,
                    user == null ? null : user.getData(),
                    amsCommentCreateDTO.getCommentContent()
            );

            log.info("留言創建成功，用戶ID: {}, 文章ID: {}", userId, articleId);





        } catch (NumberFormatException e) {
            log.error("Token 中的 userId 格式錯誤: {}", userIdFromToken, e);
            return R.error("用戶信息格式錯誤");
        } catch (Exception e) {
            log.info("留言創建失敗，請稍後再試", e);
            return R.error("留言創建失敗，請稍後再試");
        }
        return R.ok();
    }

    /**
     * 在留言建立成功後（交易提交後）送出回覆 Email 通知訊息。
     *
     * 通知規則：
     * 1、若userInfo屬性的notifyEnable總開關是關閉狀態(0)則直接跳過通知
     * 2、parentCommentId 為空：視為「文章被回覆」，通知文章作者
     * 3、parentCommentId 不為空：視為「留言被回覆」，通知父留言作者
     * 4、若回覆者與收件人為同一人，則略過通知避免自我提醒。
     */
    private void sendReplyEmailNotifyAfterCommit(
            Long articleId,
            Long commentId,
            Long parentCommentId,
            Long replierUserId,
            UserBasicDTO replierUser,
            String replyContent
    ) {
        try {
            String replierNickname = replierUser == null ? null : replierUser.getNickName();
            String replyContentPreview = buildReplyContentPreview(replyContent);

            String articleTitle = null;
            try {
                AmsArticle article = amsArticleService.getOne(new LambdaQueryWrapper<AmsArticle>()
                        .select(AmsArticle::getTitle)
                        .eq(AmsArticle::getId, articleId)
                        .last("limit 1"));
                articleTitle = article == null ? null : article.getTitle();
            } catch (Exception e) {
                log.warn("查詢文章標題失敗，將以空值寄送通知，articleId={}", articleId, e);
            }

            if (parentCommentId == null) {
                // 文章被回覆：通知文章作者
                AmsArtinfo artinfo = amsArtinfoService.getOne(new LambdaQueryWrapper<AmsArtinfo>()
                        .select(AmsArtinfo::getUserId)
                        .eq(AmsArtinfo::getArticleId, articleId)
                        .last("limit 1"));
                Long recipientUserId = artinfo == null ? null : artinfo.getUserId();

                if (recipientUserId == null) {
                    log.warn("查無文章作者，略過 Email 通知，articleId={}", articleId);
                    return;
                }
                if (recipientUserId.equals(replierUserId)) {
                    log.info("作者自行回覆文章，略過 Email 通知，articleId={}", articleId);
                    return;
                }

                ArticleRepliedEmailNotifyMessage notifyMessage = ArticleRepliedEmailNotifyMessage.builder()
                        .recipientUserId(recipientUserId)
                        .articleId(articleId)
                        .articleTitle(articleTitle)
                        .commentId(commentId)
                        .replierUserId(replierUserId)
                        .replierNickname(replierNickname)
                        .replyContent(replyContentPreview)
                        .build();

                rabbitMessageProducer.sendAfterCommit(notifyMessage);
                return;
            }

            // 留言被回覆：通知父留言作者
            AmsCommentInfo parentCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>()
                    .select(AmsCommentInfo::getUserId)
                    .eq(AmsCommentInfo::getCommentId, parentCommentId)
                    .last("limit 1"));

            Long recipientUserId = parentCommentInfo == null ? null : parentCommentInfo.getUserId();
            if (recipientUserId == null) {
                log.warn("查無父留言作者，略過 Email 通知，commentId={}, parentCommentId={}", commentId, parentCommentId);
                return;
            }
            if (recipientUserId.equals(replierUserId)) {
                log.info("使用者自行回覆留言，略過 Email 通知，commentId={}, parentCommentId={}", commentId, parentCommentId);
                return;
            }

            CommentRepliedEmailNotifyMessage notifyMessage = CommentRepliedEmailNotifyMessage.builder()
                    .recipientUserId(recipientUserId)
                    .articleId(articleId)
                    .articleTitle(articleTitle)
                    .parentCommentId(parentCommentId)
                    .commentId(commentId)
                    .replierUserId(replierUserId)
                    .replierNickname(replierNickname)
                    .replyContent(replyContentPreview)
                    .build();

            rabbitMessageProducer.sendAfterCommit(notifyMessage);
        } catch (Exception e) {
            log.warn("建立回覆 Email 通知訊息失敗（不影響留言建立），articleId={}, commentId={}, parentCommentId={}",
                    articleId, commentId, parentCommentId, e);
        }
    }

    private String buildReplyContentPreview(String content) {
        if (content == null) {
            return null;
        }
        String trimmed = content.strip();
        int maxLength = 120;
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength) + "…";
    }

//    @OpenCache(prefix = "AmsComments",key = "articleId_#{#articleId}",time = 30,chronoUnit = ChronoUnit.MINUTES)
//    @Override
//    public List<AmsArtCommentsVo> getArtComments(Long articleId) {
//
//
//        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
//        if(amsArticleService.isArticleNotExists(articleId)){
//            log.warn("文章不存在，articleId={}",articleId);
//            throw new CustomRuntimeException("文章不存在");
//        }
//
//        //根據該文章的ArticleId查找所有關聯的CommentInfo
//        List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
//        if(amsCommentInfoList.isEmpty()){
//            return null;
//        }
//        List<Long> amsCommentIds = amsCommentInfoList.stream()
//                .map(AmsCommentInfo::getCommentId)
//                .collect(Collectors.toList());
//
//
//
//
//        log.info("所有留言的Ids:{}",amsCommentIds);
//        //根據CommentInfo中的CommentId查找該文章所有的留言
//        List<AmsComment> amsCommentList = this.baseMapper.selectList(new LambdaQueryWrapper<AmsComment>().in(AmsComment::getId, amsCommentIds));
//        log.info("所有留言:{}",amsCommentList);
//        //將commentList轉為MAP屬性,KEY為ID、VALUE為對象,方便與CommentInfo的內容對應上
//        Map<Long, AmsComment> amsCommentCollect = amsCommentList.stream().collect(Collectors.toMap(AmsComment::getId, Function.identity()));
//        Map<Long, AmsCommentInfo> amsCommentInfoCollect = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getId, Function.identity()));
////        List<AmsArtCommentsVo> amsArtCommentsFromParentCommentId = getAmsArtCommentsFromParentCommentId(amsCommentInfo.getParentCommentId());
//        //該文章下所有是父留言的留言ID
////        List<Long> amsParentCommentIds = amsCommentInfoList.stream()
////                .map(AmsCommentInfo::getParentCommentId)
////                .collect(Collectors.toList());
//
//        //TODO 利用用戶的ID來獲取用戶名稱
//        List<Long> userIds = amsCommentInfoList.stream().map(AmsCommentInfo::getUserId).toList();
//        R<List<UserBasicDTO>> usersByIds = userFeignClient.selectUserBasicInfoByIds(userIds);
//
//        Map<Long,UserBasicDTO> userBasicDTOLongMap = Optional.ofNullable(usersByIds)
//                .map(R::getData)//若usersByIds不為空,則取出Data
//                .orElse(Collections.emptyList())//若Data為空,則給一個空的List
//                .stream()
//                .collect(Collectors.toMap(UserBasicDTO::getUserId, Function.identity()));//將List轉為Map,KEY為UserBasicDTO對象、VALUE為UserId
//
//
//        List<AmsArtCommentsVo> amsArtCommentsVos = amsCommentInfoList.stream().map(amsCommentInfo -> {
//            AmsArtCommentsVo amsArtCommentsVo = new AmsArtCommentsVo();
//            AmsComment amsComment = amsCommentCollect.get(amsCommentInfo.getCommentId());
//            amsArtCommentsVo.setCommentContent(amsComment.getCommentContent());
//
//
////            if(!userBasicDTOLongMap.isEmpty())
//
//            if(!userBasicDTOLongMap.isEmpty()){
//                UserBasicDTO userBasicDTO = userBasicDTOLongMap.get(amsCommentInfo.getUserId());
//                amsArtCommentsVo.setUsername(userBasicDTO.getNickName());
//                amsArtCommentsVo.setAvatar(userBasicDTO.getAvatar());
//            }
//
//            Long commentLikeCountFromRedis = getCommentLikeCountFromRedis(amsCommentInfo.getCommentId());
//            log.debug("commentLikeCountFromRedis:{}",commentLikeCountFromRedis);
////            amsArtCommentsVo.setLikesCount(commentLikeCountFromRedis.size());
//
//            amsArtCommentsVo.setLikesCount(commentLikeCountFromRedis.intValue());
//
//            amsArtCommentsVo.setrepliesCount(amsCommentInfo.getrepliesCount());
//            amsArtCommentsVo.setCreateAt(amsCommentInfo.getCreateAt());
//            amsArtCommentsVo.setUpdateAt(amsCommentInfo.getUpdateAt());
//
//            amsArtCommentsVo.setCommentId(amsCommentInfo.getCommentId());
//            if (amsCommentInfo.getParentCommentId() != null) {
//                //TODO 查詢父留言中的資料
//                //拿parentCommentIds去這篇文章中的所有留言來搜尋到父留言
//
//                AmsComment parentComment = amsCommentCollect.get(amsCommentInfo.getParentCommentId());
//                AmsCommentInfo parentCommentInfo = amsCommentInfoCollect.get(parentComment.getCommentInfoId());
//
//                log.info("留言ID:{},父留言ID:{}", amsCommentInfo.getCommentId(), parentComment.getId());
//                log.info("留言ID:{},父留言InfoID:{}", amsCommentInfo.getCommentId(), parentCommentInfo.getId());
//                log.info("留言ID:{},父留言對象:{}", amsCommentInfo.getCommentId(), parentComment);
//                log.info("留言ID:{},父留言Info對象:{}", amsCommentInfo.getCommentId(), parentCommentInfo);
//                //包裝父留言
//                AmsArtCommentsVo artParentCommentsVo = new AmsArtCommentsVo();
//                BeanUtils.copyProperties(parentComment,artParentCommentsVo);
//                BeanUtils.copyProperties(parentCommentInfo,artParentCommentsVo);
//                amsArtCommentsVo.setParentCommentId(artParentCommentsVo.getCommentId());
//
//                amsArtCommentsVo.setUsername("test");
//                log.info("留言ID:{},父留言包裝後Vo對象:{}", amsCommentInfo.getCommentId(), artParentCommentsVo);
//                return amsArtCommentsVo;
//            }
//            return amsArtCommentsVo;
//        }).collect(Collectors.toList());
//
//        log.info("amsArtCommentsVos:{}",amsArtCommentsVos);
//        return amsArtCommentsVos;
//    }

    /**
     * 查詢文章中的所有留言
     * @param articleId 文章ID
     * @return 包含文章中所有留言的靜態資訊的列表
     */
    @OpenCache(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX, key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY, time = 30, chronoUnit = ChronoUnit.MINUTES)
    @Override
    public List<AmsArtCommentsVo> getArtComments(Long articleId) {
        log.info("查詢文章中的所有留言 - articleId: {}", articleId);

        /*
        透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
         */
        amsArticleService.isArticleNotExists(articleId);
        /*
        獲取文章中留言的靜態資訊
         */
        List<AmsArtCommentStaticVo> staticCommentDetails = self.getStaticCommentDetails(articleId);
        if(staticCommentDetails.isEmpty()){
            log.warn("文章無留言, articleId:{}", articleId);
            return Collections.emptyList();
        }


        log.info("留言的靜態資訊 staticCommentDetails:{}",staticCommentDetails);
        /*
        獲取文章中留言的動態資訊(點讚數、留言數、書籤數等)
         */

        Map<String, Map<Long, Integer>> commentsMetrics = getCommentsMetrics(articleId);
        Map<Long, Integer> likesCountMap = commentsMetrics.get("likesCountMap");
        Map<Long, Integer> repliesCountMap = commentsMetrics.get("repliesCountMap");

        log.debug("文章中所有留言的指標, articleId:{} , commentsMetrics:{}",articleId,commentsMetrics);


        /*
        將文章中留言的靜態資訊和動態資訊合併
         */

        List<AmsArtCommentsVo> amsArtCommentsVoList = staticCommentDetails.stream().map(item -> {

            AmsArtCommentsVo amsArtCommentsVo = new AmsArtCommentsVo();

            //如果沒有對應的點讚數則給一個預設值-1
            int commentLikeCount = likesCountMap.getOrDefault(item.getCommentId(),-1);
            int repliesCount = repliesCountMap.getOrDefault(item.getCommentId(),-1);
            amsArtCommentsVo.setLikesCount(commentLikeCount);
            amsArtCommentsVo.setRepliesCount(repliesCount);
            BeanUtils.copyProperties(item, amsArtCommentsVo);
            return amsArtCommentsVo;
        }).toList();
        log.info("文章中所有留言合併完成,留言內容:{}",amsArtCommentsVoList);
        return amsArtCommentsVoList;

    }


    @Override
    public IPage<AmsUserCommentVo> getUserCommentsByUserId(Long userId, Integer routePage) {
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("查詢的目標用戶ID不存在")
                    .build();
        }

        if (routePage == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("查詢頁碼不可為空")
                    .build();
        }

        final int pageSize = 20;
        Page<AmsUserCommentVo> page = new Page<>(routePage, pageSize);

        log.info("查詢用戶留言列表 - userId: {}, routePage: {}", userId, routePage);
        IPage<AmsUserCommentVo> resultPage = this.baseMapper.selectUserComments(page, userId);

        if (resultPage == null || resultPage.getRecords() == null || resultPage.getRecords().isEmpty()) {
            log.info("用戶留言列表結果為空 - userId: {}, routePage: {}", userId, routePage);
            return new Page<>(routePage, pageSize);
        }

        log.debug("用戶留言分頁查詢完成，總條數:{}，當前頁:{}，每頁數量:{}", resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
        return resultPage;
    }


    /**
     * 查詢文章中的所有留言
     * @param articleId 文章ID
     * @return 包含文章中所有留言的靜態資訊的列表
     */
    @OpenCache(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX, key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY, time = 30, chronoUnit = ChronoUnit.MINUTES)//
    @Override
    public List<AmsArtCommentStaticVo> getStaticCommentDetails(Long articleId) {
        log.info("從資料庫中查詢文章中的所有留言, articleId={}",articleId);
        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
        amsArticleService.isArticleNotExists(articleId);

        List<AmsArtCommentStaticVo> amsArtCommentStaticVos = this.baseMapper.getStaticCommentDetails(articleId);

//        //根據該文章的ArticleId查找所有關聯的CommentInfo
//        List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
//        if(amsCommentInfoList.isEmpty()){
//            //假設文章無留言, 則直接回傳空陣列
//            log.info("文章無留言, articleId:{}", articleId);
//            return Collections.emptyList();
//        }
//        List<Long> amsCommentIds = amsCommentInfoList.stream()
//                .map(AmsCommentInfo::getCommentId)
//                .collect(Collectors.toList());
//
//
//
//
//        log.info("所有留言的Ids:{}",amsCommentIds);
//        //根據CommentInfo中的CommentId查找該文章所有的留言
//        List<AmsComment> amsCommentList = this.baseMapper.selectList(new LambdaQueryWrapper<AmsComment>().in(AmsComment::getId, amsCommentIds));
//        log.info("所有留言:{}",amsCommentList);
//        //將commentList轉為MAP屬性,KEY為ID、VALUE為對象,方便與CommentInfo的內容對應上
//        Map<Long, AmsComment> amsCommentCollect = amsCommentList.stream().collect(Collectors.toMap(AmsComment::getId, Function.identity()));
//        Map<Long, AmsCommentInfo> amsCommentInfoCollect = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getId, Function.identity()));
////        List<AmsArtCommentStaticVo> amsArtCommentsFromParentCommentId = getAmsArtCommentsFromParentCommentId(amsCommentInfo.getParentCommentId());
//        //該文章下所有是父留言的留言ID
////        List<Long> amsParentCommentIds = amsCommentInfoList.stream()
////                .map(AmsCommentInfo::getParentCommentId)
////                .collect(Collectors.toList());
//
//        //TODO 利用用戶的ID來獲取用戶名稱
//        List<Long> userIds = amsCommentInfoList.stream().map(AmsCommentInfo::getUserId).toList();
////        R<List<UserBasicDTO>> usersByIds = userFeignClient.selectUserBasicInfoByIds(userIds);
//
////        Map<Long,UserBasicDTO> userBasicDTOLongMap = Optional.ofNullable(usersByIds)
////                .map(R::getData)//若usersByIds不為空,則取出Data
////                .orElse(Collections.emptyList())//若Data為空,則給一個空的List
////                .stream()
////                .collect(Collectors.toMap(UserBasicDTO::getUserId, Function.identity()));//將List轉為Map,KEY為UserBasicDTO對象、VALUE為UserId
//
//
//        List<AmsArtCommentStaticVo> amsArtCommentStaticVos = amsCommentInfoList.stream().map(amsCommentInfo -> {
//            AmsArtCommentStaticVo amsArtCommentsStaticVo = new AmsArtCommentStaticVo();
//            AmsComment amsComment = amsCommentCollect.get(amsCommentInfo.getCommentId());
//            amsArtCommentsStaticVo.setCommentContent(amsComment.getCommentContent());
//
//
////            if(!userBasicDTOLongMap.isEmpty())
//
////            if(!userBasicDTOLongMap.isEmpty()){
////                UserBasicDTO userBasicDTO = userBasicDTOLongMap.get(amsCommentInfo.getUserId());
////                amsArtCommentsStaticVo.setUsername(userBasicDTO.getNickName());
////                amsArtCommentsStaticVo.setAvatar(userBasicDTO.getAvatar());
////            }
//
//            amsArtCommentsStaticVo.setCreateAt(amsCommentInfo.getCreateAt());
//            amsArtCommentsStaticVo.setUpdateAt(amsCommentInfo.getUpdateAt());
//
//            amsArtCommentsStaticVo.setCommentId(amsCommentInfo.getCommentId());
//
//            amsArtCommentsStaticVo.setNickName(amsCommentInfo.getNickName());
//            amsArtCommentsStaticVo.setAvatar(amsCommentInfo.getAvatar());
//
//            if (amsCommentInfo.getParentCommentId() != null) {
//                //TODO 查詢父留言中的資料
//                //拿parentCommentIds去這篇文章中的所有留言來搜尋到父留言
//
//                AmsComment parentComment = amsCommentCollect.get(amsCommentInfo.getParentCommentId());
//                AmsCommentInfo parentCommentInfo = amsCommentInfoCollect.get(parentComment.getCommentInfoId());
//
//                log.info("留言ID:{},父留言ID:{}", amsCommentInfo.getCommentId(), parentComment.getId());
//                log.info("留言ID:{},父留言InfoID:{}", amsCommentInfo.getCommentId(), parentCommentInfo.getId());
//                log.info("留言ID:{},父留言對象:{}", amsCommentInfo.getCommentId(), parentComment);
//                log.info("留言ID:{},父留言Info對象:{}", amsCommentInfo.getCommentId(), parentCommentInfo);
//                //包裝父留言
//                AmsArtCommentsVo artParentCommentsVo = new AmsArtCommentsVo();
//                BeanUtils.copyProperties(parentComment,artParentCommentsVo);
//                BeanUtils.copyProperties(parentCommentInfo,artParentCommentsVo);
//                amsArtCommentsStaticVo.setParentCommentId(artParentCommentsVo.getCommentId());
//
//                amsArtCommentsStaticVo.setNickName(amsCommentInfo.getNickName());
//                amsArtCommentsStaticVo.setAvatar(amsCommentInfo.getAvatar());
//
//                log.info("留言ID:{},父留言包裝後Vo對象:{}", amsCommentInfo.getCommentId(), artParentCommentsVo);
//                return amsArtCommentsStaticVo;
//            }
//            return amsArtCommentsStaticVo;
//        }).collect(Collectors.toList());

        log.info("amsArtCommentsVos:{}",amsArtCommentStaticVos);
        return amsArtCommentStaticVos;
    }




    /**
     * 判斷文章是否不存在，透過布隆過濾器以及資料庫雙重確認，非分佈式鎖版本
     * @param commentId 留言ID
     * @return 是否文章不存在 , 若不存在則返回true;若存在則返回false
     */
    public boolean isCommentNotExists(Long commentId) {

        if (commentId == null || commentId <= 0) {
            log.warn("非法留言ID: {}", commentId);
            return true;
        }

        /**
         * 透過布隆過濾器初步判斷該留言是否存在
         */

        try {
            /// TODO增加 Bloom 就緒旗標，透過旗標判斷是否需要檢查布隆過濾器，避免Redis服務異常導致無法使用布隆過濾器
            String commentBloomFilterPattern = RedisBloomFilterKey.COMMENT_BLOOM_FILTER.getKey();
            RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(commentBloomFilterPattern);

            if(!bloomFilter.contains(commentId)){
                //若果布隆過濾器中不存在該留言ID，表示該留言一定不存在
                log.info("該留言ID:{}不存在或已被刪除", commentId);
                return true;
            }
        } catch (RedisConnectionException e) {
            log.error("Redis 連線異常，跳過布隆過濾器檢查，commentId={}", commentId, e);
            return existsCommentIdFromDB(commentId);

        }


        return false;

    }


    /**
     * 點讚留言
     * @param articleId 文章ID
     * @param commentId 留言ID
     * @return 按讚數
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer likeComment(Long articleId,Long commentId) {
        /// TODO同步點讚數從Redis到資料庫中


        //透過布隆過濾器判斷該留言是否不存在, 若不存在則拋出異常
        if(this.isCommentNotExists(commentId)){
//            log.info("留言不存在，commentId={}",commentId);
//            throw new CustomRuntimeException("留言不存在");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言不存在")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId,""),
                            "commentId", ObjectUtils.defaultIfNull(commentId,"")
                            ))
                    .build();
        }

        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕對留言按讚")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();

        if (userId == null) {
//            log.warn("用戶ID為空，拒絕對留言按讚");
//            throw new CustomRuntimeException("用戶ID缺失");
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶不存在，拒絕對留言按讚")
                    .data(Map.of(
                            "articleId",ObjectUtils.defaultIfNull(articleId,""),
                            "commentId",ObjectUtils.defaultIfNull(commentId,"")
                            ))
                    .build();
        }

        final String userLikedSetKey = RedisCacheKey.COMMENT_LIKED_USERS.format(commentId);
        final String commentLikesHashKey = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.format(articleId);


        /**
         * 檢查用戶是否已經點讚過該留言
         * 先檢查Redis中的點讚用戶集合
         */



//        RSet<String> likedUsersSet = redissonClient.getSet(userLikedSetKey);
//
//        if (likedUsersSet.contains(userId.toString())) {
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.REPEAT_OPERATION)
//                    .detailMessage("用戶已經點讚過該留言, 不允許重複點讚")
//                    .data(Map.of(
//                            "userId", ObjectUtils.defaultIfNull(userId, ""),
//                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
//                            "commentId", ObjectUtils.defaultIfNull(commentId, "")
//                    ))
//                    .build();
//        }

        RScript containsRScript = redissonClient.getScript(StringCodec.INSTANCE);

        Long contains = containsRScript.eval(
                RScript.Mode.READ_ONLY,
                RedisLuaScripts.CHECK_VALUE_IN_SET_SCRIPT,
                RScript.ReturnType.INTEGER,
                List.of(userLikedSetKey),
                userId.toString()
        );
        if(contains != 0){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶已經點讚過該留言, 不允許重複點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "commentId", ObjectUtils.defaultIfNull(commentId, "")
                    ))
                    .build();
        }

        /*
          判斷該留言是否存在
        */
        //獲取桶對象

        RMap<Long, Integer> commentLikesHash = redissonClient.getMap(commentLikesHashKey);

        //先嘗試從Redis中讀取是否存在該留言的緩存
        //若快取中不存在該留言ID，則從資料庫中讀取該留言是否存在，若存在則新增至快取中
        if(!commentLikesHash.containsKey(commentId)){
            //若不存在則根據commentId從資料庫中讀取該留言是否存在
            Boolean commentIdFromDB = this.existsCommentIdFromDB(commentId);
            if(commentIdFromDB){

                // 從 DB 查詢留言信息（包含點讚數）
                AmsCommentStatistics amsCommentStatistics = amsCommentStatisticsService.getOne(new LambdaQueryWrapper<AmsCommentStatistics>().select(AmsCommentStatistics::getCommentId,AmsCommentStatistics::getLikesCount).eq(AmsCommentStatistics::getCommentId, commentId));

                if(amsCommentStatistics == null){

//                    throw new CustomRuntimeException("該留言不存在或已被刪除");
                    throw BusinessRuntimeException.builder()
                            .iErrorCode(ResultCode.NOT_FOUND)
                            .data("留言不存在")
                            .data(Map.of(
                                    "articleId",ObjectUtils.defaultIfNull(articleId,""),
                                    "commentId",ObjectUtils.defaultIfNull(commentId,"")

                                    ))
                            .build();
                }
                // 從 DB 查詢成功得到該留言的資訊
                // 初始化緩存，將該留言的點讚數加入緩存
                commentLikesHash.put(amsCommentStatistics.getCommentId(), amsCommentStatistics.getLikesCount());

            }


        }

        try {
            RScript rScript = redissonClient.getScript(StringCodec.INSTANCE);

            Long luaResult = rScript.eval(
                    RScript.Mode.READ_WRITE, //寫入模式
                    RedisLuaScripts.LIKE_COMMENT_SCRIPT,//腳本
                    RScript.ReturnType.INTEGER, //返回值
                    Arrays.asList(userLikedSetKey, commentLikesHashKey),
                    userId.toString(), //argv[1]
                    commentId.toString() //argv[2]
            );

            if (luaResult == null) {
//                log.error("Lua文章中的留言點讚腳本返回null,用戶ID:{},留言ID:{}", userId, commentId);
//                throw new CustomException("系統異常");
                throw BusinessException.builder()
                        .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                        .detailMessage("Lua腳本異常返回null")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId,""),
                                "commentId", ObjectUtils.defaultIfNull(commentId,"")
                        ))
                        .build();
            }

            if (luaResult > 0) {
                log.info("點讚成功,用戶ID:{},留言ID:{},新的按讚數:{}", userId, commentId, luaResult);

                /**
                 *  發送 RabbitMQ 消息更新 AmsCommentAction 和 AmsCommentStatistics
                 *  改為異步處理，提高響應速度
                 */
                
                // 發送留言點讚數更新消息 (delta = +1)
                UpdateCommentLikedMessage likedMessage = UpdateCommentLikedMessage.builder()
                        .commentId(commentId)
                        .delta(1)
                        .build();
                rabbitMessageProducer.send(likedMessage);
                
                // 發送用戶留言互動行為消息 (isLiked = 1)
                UpdateCommentActionMessage actionMessage = UpdateCommentActionMessage.builder()
                        .commentId(commentId)
                        .articleId(articleId)
                        .userId(userId)
                        .isLiked((byte) 1)
                        .build();
                rabbitMessageProducer.send(actionMessage);

                // 返回新的按讚數
                return Math.toIntExact(luaResult);
            } else if (luaResult == -1) {
//                log.warn("重複點讚,用戶ID:{},留言ID:{}", userId, commentId);
//                throw new CustomRuntimeException("您已經點過讚了");

                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.REPEAT_OPERATION)
                        .detailMessage("用戶已經點讚過該留言, 不允許重複點讚")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId,""),
                                "commentId", ObjectUtils.defaultIfNull(commentId,"")

                        ))
                        .build();
            } else {
//                log.error("Lua腳本異常返回值:{}", luaResult);
//                throw new CustomRuntimeException("系統異常");
                throw BusinessException.builder()
                        .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                        .detailMessage("Lua腳本異常返回值")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId,""),
                                "commentId", ObjectUtils.defaultIfNull(commentId,""),
                                "invalidResult", ObjectUtils.defaultIfNull(luaResult, "null")
                                ))
                        .build();
            }
        } catch (RedisException e) {
//            log.error("Redis執行異常,用戶ID:{},留言ID:{}", userId, commentId, e);
//            throw new CustomRuntimeException("服務暫時不可用，請稍後再試");
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                    .detailMessage("Lua腳本異常返回值")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId,""),
                            "commentId", ObjectUtils.defaultIfNull(commentId,"")
                    ))
                    .build();
        }
    }

    /**
     * 取消點讚留言
     * @param articleId 文章ID
     * @param commentId 留言ID
     * @return 新的點讚數
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Integer unlikeComment(Long articleId, Long commentId) {
        log.info("開始取消留言點讚，文章ID={}，留言ID={}", articleId, commentId);

        //透過布隆過濾器判斷該留言是否不存在, 若不存在則拋出異常
        if(this.isCommentNotExists(commentId)){
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言不存在")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId,""),
                            "commentId", ObjectUtils.defaultIfNull(commentId,"")
                    ))
                    .build();
        }

        /*
          檢查用戶是否登入
        */
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，拒絕取消留言按讚")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();

        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶不存在，拒絕取消留言按讚")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId,""),
                            "commentId", ObjectUtils.defaultIfNull(commentId,"")
                    ))
                    .build();
        }

        log.info("用戶取消留言點讚, 用戶ID:{} , 文章ID:{}, 留言ID:{}", userId, articleId, commentId);

        final String userLikedSetKey = RedisCacheKey.COMMENT_LIKED_USERS.format(commentId);
        final String commentLikesHashKey = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.format(articleId);

        /**
         * 檢查用戶是否已經點讚過該留言
         * 先檢查Redis中的點讚用戶集合
         */
//        RSet<String> likedUsersSet = redissonClient.getSet(userLikedSetKey, StringCodec.INSTANCE);
//
//        if (!likedUsersSet.contains(userId.toString())) {
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.REPEAT_OPERATION)
//                    .detailMessage("用戶尚未點讚該留言, 無法取消點讚")
//                    .data(Map.of(
//                            "userId", ObjectUtils.defaultIfNull(userId, ""),
//                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
//                            "commentId", ObjectUtils.defaultIfNull(commentId, "")
//                    ))
//                    .build();
//        }


        /**
         *  檢查用戶是否已經點讚過該留言
         */
        RScript containsRScript = redissonClient.getScript(StringCodec.INSTANCE);

        Long contains = containsRScript.eval(
                RScript.Mode.READ_ONLY,
                RedisLuaScripts.CHECK_VALUE_IN_SET_SCRIPT,
                RScript.ReturnType.INTEGER,
                List.of(userLikedSetKey),
                userId.toString()
        );

        if (contains == null) {
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                    .detailMessage("Lua腳本異常返回null")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId,""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "commentId", ObjectUtils.defaultIfNull(commentId,"")
                    ))
                    .build();
        } else if (contains == 0) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.REPEAT_OPERATION)
                    .detailMessage("用戶尚未點讚該留言, 無法取消點讚")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "commentId", ObjectUtils.defaultIfNull(commentId, "")
                    ))
                    .build();
        }



        log.debug("用戶曾經點讚過該留言,userId: {}, articleId:{} , commentId:{}",userId,articleId,commentId);

        /**
         * Redis操作
         * 執行 Lua 腳本：將用戶從點讚集合移除並減少點讚數
         */
        try {
            RScript rScript = redissonClient.getScript(StringCodec.INSTANCE);

            Long luaResult = rScript.eval(
                    RScript.Mode.READ_WRITE,
                    RedisLuaScripts.UNLIKE_COMMENT_SCRIPT,
                    RScript.ReturnType.INTEGER,
                    Arrays.asList(userLikedSetKey, commentLikesHashKey),
                    userId.toString(),
                    commentId.toString()
            );

            if (luaResult == null) {
                throw BusinessException.builder()
                        .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                        .detailMessage("Lua腳本異常返回null")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId,""),
                                "articleId", ObjectUtils.defaultIfNull(articleId,""),
                                "commentId", ObjectUtils.defaultIfNull(commentId,"")
                        ))
                        .build();
            }

            if (luaResult >= 0) {
                log.info("取消點讚成功,用戶ID:{},留言ID:{},新的按讚數:{}", userId, commentId, luaResult);

                /**
                 *  發送 RabbitMQ 消息更新 AmsCommentAction 和 AmsCommentStatistics
                 *  改為異步處理，提高響應速度
                 */
                
                // 發送留言點讚數更新消息 (delta = -1)
                UpdateCommentLikedMessage likedMessage = UpdateCommentLikedMessage.builder()
                        .commentId(commentId)
                        .delta(-1)
                        .build();
                rabbitMessageProducer.send(likedMessage);
                
                // 發送用戶留言互動行為消息 (isLiked = 0)
                UpdateCommentActionMessage actionMessage = UpdateCommentActionMessage.builder()
                        .commentId(commentId)
                        .articleId(articleId)
                        .userId(userId)
                        .isLiked((byte) 0)
                        .build();
                rabbitMessageProducer.send(actionMessage);

                // 返回新的按讚數
                return Math.toIntExact(luaResult);
            } else if (luaResult == -1) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.REPEAT_OPERATION)
                        .detailMessage("用戶尚未點讚該留言, 無法取消點讚")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId,""),
                                "articleId", ObjectUtils.defaultIfNull(articleId,""),
                                "commentId", ObjectUtils.defaultIfNull(commentId,"")
                        ))
                        .build();
            } else {
                throw BusinessException.builder()
                        .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                        .detailMessage("Lua腳本異常返回值")
                        .data(Map.of(
                                "userId", ObjectUtils.defaultIfNull(userId,""),
                                "articleId", ObjectUtils.defaultIfNull(articleId,""),
                                "commentId", ObjectUtils.defaultIfNull(commentId,""),
                                "invalidResult", ObjectUtils.defaultIfNull(luaResult, "null")
                        ))
                        .build();
            }
        } catch (RedisException e) {
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.REDIS_INTERNAL_ERROR)
                    .detailMessage("Redis執行異常")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId,""),
                            "articleId", ObjectUtils.defaultIfNull(articleId,""),
                            "commentId", ObjectUtils.defaultIfNull(commentId,"")
                    ))
                    .build();
        }
    }

    /**
     * 從DB中判斷留言是否存在
     * @param commentId 留言ID
     * @return {@code true} 如果留言存在且唯一; {@code false} 如果留言ID無效、不存在或已被刪除
     */
    @Override
    public Boolean existsCommentIdFromDB(Long commentId) {
        if (commentId == null || commentId <= 0) {
            log.warn("非法留言ID: {}", commentId);
            return false;
        }
        Long count = this.baseMapper.selectCount((new LambdaQueryWrapper<AmsComment>().eq(AmsComment::getId, commentId)));
        if(count!=1){
            log.warn("該留言ID:{}不存在或已被刪除",commentId);
            return false;
        }

        return true;

    }

    /**
     * 取得文章中留言的點讚數聚合（Hash: field=commentId, value=likesCount）
     * 先從Redis中取得，如果不存在則從DB中取得，再將DB中的資料加入Redis中進行快取
     * @param articleId 文章ID
     * @return 包含留言ID到按讚數和回覆數的映射
     */
    public Map<String, Map<Long, Integer>> getCommentsMetrics(Long articleId) {
        log.info("開始執行 getCommentsMetrics - articleId: {}", articleId);

        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
        amsArticleService.isArticleNotExists(articleId);

        final String likesCountBucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern(),articleId);
        final String repliesCountBucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_REPLIES_COUNT_HASH.getPattern(),articleId);

        //先嘗試從Redis中讀取文章中所有留言的指標資料
        Map<String, Map<Long, Integer>> commentsMetricMap = this.parseCommentsMetric(articleId);
        //判斷是否成功從Redis中讀取
        Map<Long, Integer> likesCountMap = commentsMetricMap.get("likesCountMap");
        Map<Long, Integer> repliesCountMap = commentsMetricMap.get("repliesCountMap");

        boolean needLoadFromDB = likesCountMap.isEmpty() || repliesCountMap.isEmpty();
        log.debug("Redis 快取狀態 - articleId: {}, needLoadFromDB: {}, likesCount: {}, repliesCount: {}",
                articleId, needLoadFromDB, likesCountMap.size(), repliesCountMap.size());
        if(needLoadFromDB){
            //假設未成功從Redis中讀取則調用資料庫
            log.info("Redis 快取未命中,從資料庫載入 - articleId: {}", articleId);
            commentsMetricMap = redisCacheLoaderUtils.loadMapWithLock(

                    () -> loadCommentsMetric(articleId),
                    () -> parseCommentsMetric(articleId),
                    3,
                    10,
                    TimeUnit.SECONDS,
                    3,
                    RedisLockKey.ARTICLE_COMMENTS_LIKES_LOCK.getFormat(articleId),
                    likesCountBucketName,repliesCountBucketName


            );
            log.info("成功從資料庫載入並快取文章所有留言的指標 - articleId: {}", articleId);
        }else{
            log.info("成功從 Redis 讀取文章所有留言的指標 - articleId: {}", articleId);
        }


        return commentsMetricMap;

    }



    /**
     * 從資料庫中獲取文章中所有留言的指標，並寫入至快取中
     * @param articleId 文章ID
     * @return 包含留言ID到按讚數和回覆數的映射
     */
    public Map<String,Map<Long,Integer>> loadCommentsMetric(Long articleId){
        log.info("開始執行獲取文章中所有留言的指標 loadCommentsMetric - articleId: {}", articleId);

        log.debug("執行資料庫查詢 - articleId: {}", articleId);
        List<AmsCommentStatistics> amsCommentStatistics = QueryCommentsMetric(articleId);
        log.info("資料庫查詢完成 - articleId: {}, 留言數量: {}", articleId, amsCommentStatistics.size());
        final String likesCountBucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern(),articleId);
        final String repliesCountBucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_REPLIES_COUNT_HASH.getPattern(),articleId);

        log.debug("Redis Key 資訊 - likesKey: {}, repliesKey: {}", likesCountBucketName, repliesCountBucketName);
        //判斷是否成功從資料庫中取得該文章所有留言的指標
        if(amsCommentStatistics.isEmpty()){
            /*
            假設未成功從資料庫中取得該文章所有留言的指標，則寫入空快取，避免快取穿透，並設置TTL為3分鐘
             */
            log.warn("資料庫無留言資料,寫入空快取標記防止快取穿透 - articleId: {}, TTL: 3分鐘", articleId);
            //創建批次
            RBatch putBatch = redissonClient.createBatch();


            RMapAsync<Long, Integer> putLikesMapAsync = putBatch.getMap(likesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));
            RMapAsync<Long, Integer> putRepliesMapAsync = putBatch.getMap(repliesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));
            //創建空快取標記
            Map<@NotNull Long, Integer> likesMap = new HashMap<>();
            likesMap.put(-1L, -1);  // 創建空緩存標記
            Map<@NotNull Long, Integer> repliesMap = new HashMap<>();
            repliesMap.put(-1L, -1);  // 創建空緩存標記


            //將快取標記寫入資料庫, 過期時間為3分鐘
            putLikesMapAsync.putAllAsync(likesMap);
            putLikesMapAsync.expireAsync(Duration.ofMinutes(3));
            putRepliesMapAsync.putAllAsync(repliesMap);
            putRepliesMapAsync.expireAsync(Duration.ofMinutes(3));
            //執行批次實現空快取標記
            putBatch.execute();

            Map<String,Map<Long,Integer>> result = new HashMap<>();
            //從資料庫中取得該文章的留言點讚數

            //包裝成目標對象, 其中內容包含空快取標記-1L

            result.put("likesCountMap",likesMap);
            result.put("repliesCountMap",repliesMap);

            log.warn("資料庫查詢無留言資料,寫入空快取標記防止快取穿透 - articleId: {}",articleId);
            return result;
        }
        //假設成功從資料庫中取得該文章所有留言的指標，則寫入快取

        log.debug("處理留言指標資料 - article: {}, amsCommentStatistics: {}",articleId,amsCommentStatistics);

        Map<@NotNull Long, Integer> likesMap = amsCommentStatistics.stream().collect(Collectors.toMap(AmsCommentStatistics::getCommentId, AmsCommentStatistics::getLikesCount));
        Map<@NotNull Long, Integer> repliesMap = amsCommentStatistics.stream().collect(Collectors.toMap(AmsCommentStatistics::getCommentId, AmsCommentStatistics::getRepliesCount));
        Map<String,Map<Long,Integer>> result = new HashMap<>();

        //包裝成目標對象
        result.put("likesCountMap",likesMap);
        result.put("repliesCountMap",repliesMap);



        RBatch putBatch = redissonClient.createBatch();
        RMapAsync<Long, Integer> putLikesMapAsync = putBatch.getMap(likesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));
        RMapAsync<Long, Integer> putRepliesMapAsync = putBatch.getMap(repliesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));
        //將快取標記寫入資料庫
        putLikesMapAsync.putAllAsync(likesMap);
        putRepliesMapAsync.putAllAsync(repliesMap);
        putLikesMapAsync.expireAsync(RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getTtl());
        putRepliesMapAsync.expireAsync(RedisCacheKey.ARTICLE_COMMENT_REPLIES_COUNT_HASH.getTtl());
        //執行批次實現快取
        putBatch.execute();


        log.info("成功獲取文章中所有留言的指標並且入至快取中 LoadCommentsMetric - articleId: {}", articleId);

        return result;
    }

    /**
     * 從資料庫中取得文章中所有留言的留言ID、點讚數、回覆數
     * @param articleId 文章ID
     * @return 包含留言ID到按讚數和回覆數的映射
     */
    public List<AmsCommentStatistics> QueryCommentsMetric(Long articleId)  {
        log.info("開始執行 QueryCommentsMetric - articleId: {}", articleId);
        //從資料庫中取得該文章的留言點讚數

        return amsCommentStatisticsService.list(new LambdaQueryWrapper<AmsCommentStatistics>()
                .eq(AmsCommentStatistics::getArticleId, articleId)
                .select(AmsCommentStatistics::getCommentId,AmsCommentStatistics::getLikesCount,AmsCommentStatistics::getRepliesCount)
        );
    }

    /**
     * 從Redis中取得文章中所有留言的留言ID、點讚數、回覆數，並包裝成
     * @param articleId 文章ID
     * @return 包含留言ID到按讚數和回覆數的映射
     */
    public Map<String,Map<Long,Integer>> parseCommentsMetric(Long articleId){
        log.info("開始從 Redis 解析文章中所有留言的指標 - articleId: {}", articleId);

        try {
            final String likesCountBucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern(),articleId);
            final String repliesCountBucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_REPLIES_COUNT_HASH.getPattern(),articleId);

            log.debug("Redis Key 資訊 - likesKey: {}, repliesKey: {}", likesCountBucketName, repliesCountBucketName);

            RBatch batch = redissonClient.createBatch();
            RMapAsync<Long, Integer> likesMapAsync = batch.getMap(likesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));
            RMapAsync<Long, Integer> repliesMapAsync = batch.getMap(repliesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));

            RFuture<Map<Long, Integer>> likesFuture = likesMapAsync.readAllMapAsync();
            RFuture<Map<Long, Integer>> repliesFuture = repliesMapAsync.readAllMapAsync();

            batch.execute();
            Map<Long, Integer> likesMap = likesFuture.get();
            Map<Long, Integer> repliesMap = repliesFuture.get();
            log.debug("解析留言指標資料 - articleId: {}, 點讚指標數量: {}, 回覆指標數量: {}",articleId,likesMap.size(),repliesMap.size());

            Map<String,Map<Long,Integer>> result = new HashMap<>();

            //包裝成目標對象

            result.put("likesCountMap",likesMap.isEmpty()? Collections.emptyMap() : likesMap);
            result.put("repliesCountMap",repliesMap.isEmpty()? Collections.emptyMap() : repliesMap);

            log.info("成功解析文章中所有留言的指標 - articleId: {}, 結果數量: {}",articleId,result.size());
            return result;
        } catch (InterruptedException | ExecutionException e) {
//            log.error("解析文章中所有留言的指標失敗 - articleId: {}", articleId, e);
//            throw new CustomRuntimeException(e.getMessage());
            throw BusinessException.builder()
                    .iErrorCode(ResultCode.ARTICLE_INTERNAL_ERROR)
                    .detailMessage("解析文章中所有留言的指標失敗")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }
    }

    /**
     * 刪除留言
     * @param articleId 文章ID
     * @param commentId 留言ID
     * @return 刪除結果
     */
    @Transactional(rollbackFor = Exception.class)
    @DelayDoubleDelete(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX, key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY)
    @Override
    public R<Void> deleteComment(Long articleId, Long commentId) {
        log.info("開始邏輯刪除留言 - articleId: {}, commentId: {}", articleId, commentId);

        //驗證文章是否存在
        redisBloomFilterUtils.requireExists(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey(), articleId, "文章不存在");

        //驗證留言是否存在
        if (this.isCommentNotExists(commentId)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言不存在")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "commentId", ObjectUtils.defaultIfNull(commentId, "")
                    ))
                    .build();
        }

        //檢查用戶是否登入
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，無法刪除留言")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();

        //驗證用戶權限：只有留言作者才能刪除
        AmsCommentInfo commentInfo = amsCommentInfoService.getOne(
                new LambdaQueryWrapper<AmsCommentInfo>()
                        .eq(AmsCommentInfo::getCommentId, commentId)
        );

        if (commentInfo == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言信息不存在")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //檢查留言是否已被刪除
        if (commentInfo.getDeleted() != null && commentInfo.getDeleted() == 1) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.DELETE_FAILED)
                    .detailMessage("留言已被刪除")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        if (!commentInfo.getUserId().equals(userId)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.FORBIDDEN)
                    .detailMessage("無權限刪除他人留言")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "commentOwnerId", ObjectUtils.defaultIfNull(commentInfo.getUserId(), "")
                    ))
                    .build();
        }

        //執行邏輯刪除 - 更新 deleted 字段為 1 , 代表刪除
        boolean logicalDelete = amsCommentInfoService.update(
                new LambdaUpdateWrapper<AmsCommentInfo>()
                        .eq(AmsCommentInfo::getCommentId, commentId)
                        .set(AmsCommentInfo::getDeleted, 1)
                        .set(AmsCommentInfo::getUpdateAt, LocalDateTime.now())
        );

        if (!logicalDelete) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("邏輯刪除留言失敗")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //邏輯刪除留言統計數據 - 更新 AmsCommentStatistics 的 deleted 字段為 1
        boolean deleteStatistics = amsCommentStatisticsService.update(
                new LambdaUpdateWrapper<AmsCommentStatistics>()
                        .eq(AmsCommentStatistics::getCommentId, commentId)
                        .set(AmsCommentStatistics::getDeleted, 1)
        );
        if (!deleteStatistics) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("邏輯刪除留言統計數據失敗")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //邏輯刪除留言互動記錄 - 更新 AmsCommentAction 的 deleted 字段為 1
        boolean deleteActions = amsCommentActionService.update(
                new LambdaUpdateWrapper<AmsCommentAction>()
                        .eq(AmsCommentAction::getCommentId, commentId)
                        .set(AmsCommentAction::getDeleted, 1)
        );
        if (!deleteActions) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("邏輯刪除留言互動記錄失敗")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //查詢該留言的回覆數量（用於更新文章留言總數）
        AmsCommentStatistics commentStatistics = amsCommentStatisticsService.getOne(
                new LambdaQueryWrapper<AmsCommentStatistics>()
                        .eq(AmsCommentStatistics::getCommentId, commentId)
                        .select(AmsCommentStatistics::getRepliesCount)
        );
        Long repliesCount = (commentStatistics != null && commentStatistics.getRepliesCount() != null)
                ? commentStatistics.getRepliesCount()
                : 0L;

        //如果有父留言，需要更新父留言的回覆數
        if (commentInfo.getParentCommentId() != null) {
            boolean updateParent = amsCommentStatisticsService.update(
                    new LambdaUpdateWrapper<AmsCommentStatistics>()
                            .eq(AmsCommentStatistics::getCommentId, commentInfo.getParentCommentId())
                            .setSql("replies_count = GREATEST(replies_count - 1, 0)")
            );
            if (!updateParent) {
//                log.warn("更新父留言回覆數失敗 - parentCommentId: {}", commentInfo.getParentCommentId());
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.UPDATE_FAILED)
                        .detailMessage("更新父留言回覆數失敗")
                        .data(Map.of("parentCommentId", ObjectUtils.defaultIfNull(commentInfo.getParentCommentId(), "")))
                        .build();
            }
        }

        //更新文章留言總數（需要減去該留言及其所有子留言）
        int totalDeleteCount = 1 + repliesCount.intValue();
        boolean updateArticleCommentCount = amsArtStatusService.update(
                new LambdaUpdateWrapper<AmsArtStatus>()
                        .eq(AmsArtStatus::getArticleId, articleId)
                        .setSql("comments_count = GREATEST(comments_count - " + totalDeleteCount + ", 0)")
        );
        if (!updateArticleCommentCount) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("更新文章留言數失敗")
                    .data(Map.of("articleId", ObjectUtils.defaultIfNull(articleId, "")))
                    .build();
        }

        /**
         * 清理 Redis 中的留言相關快取
         * Redis 操作失敗不影響資料庫邏輯刪除，但需要記錄日誌
         */

        try {
            final String likedUsersKey = RedisCacheKey.COMMENT_LIKED_USERS.format(commentId);
            final String markedUsersKey = RedisCacheKey.COMMENT_MARKED_USERS.format(commentId);
            final String commentLikesHashKey = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.format(articleId);
            final String commentRepliesHashKey = RedisCacheKey.ARTICLE_COMMENT_REPLIES_COUNT_HASH.format(articleId);

            RScript rScript = redissonClient.getScript(StringCodec.INSTANCE);
            Long luaResult = rScript.eval(
                    RScript.Mode.READ_WRITE,
                    RedisLuaScripts.DELETE_COMMENT_SCRIPT,
                    RScript.ReturnType.INTEGER,
                    Arrays.asList(likedUsersKey, markedUsersKey, commentLikesHashKey, commentRepliesHashKey),
                    commentId.toString()
            );

            if (luaResult == null || luaResult != 1) {
                log.warn("Lua 腳本清理留言 Redis 數據返回異常 - commentId: {}, result: {}", commentId, luaResult);

            } else {
                log.info("成功清理留言 Redis 緩存數據 - commentId: {}", commentId);
            }

            // 11. 更新 Redis 中的文章留言總數
            redisIncrementUtils.afterCommitDecrement(
                    RedisCacheKey.ARTICLE_COMMENTS.format(articleId),
                    totalDeleteCount
            );

        } catch (RedisException e) {
            log.error("清理留言 Redis 緩存失敗 - commentId: {}", commentId, e);


        }

        //無法從留言的布隆過濾器中移除留言, 需要注意, 仍然會保留在布隆過濾器中, 但是, 由於布隆過濾器的特性, 不會影響到其他操作

        log.info("留言邏輯刪除成功 - articleId: {}, commentId: {}, 影響留言數: {}", articleId, commentId, totalDeleteCount);

        return R.ok();
    }

    /**
     * 編輯留言
     * @param articleId 文章ID
     * @param amsCommentEditDTO 編輯留言DTO
     * @return 編輯結果
     */
    @Override
    @DelayDoubleDelete(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX,
            key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY)
    @Transactional(rollbackFor = Exception.class)
    public R<Void> editComment(Long articleId, AmsCommentEditDTO amsCommentEditDTO) {
        Long commentId = amsCommentEditDTO.getCommentId();
        String newContent = amsCommentEditDTO.getCommentContent();

        log.info("開始編輯留言 - articleId: {}, commentId: {}", articleId, commentId);
        if(newContent.isBlank()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("留言內容不能為空")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }


        //驗證文章是否存在
        redisBloomFilterUtils.requireExists(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey(), articleId, "文章不存在");

        //驗證留言是否存在
        if (this.isCommentNotExists(commentId)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言不存在")
                    .data(Map.of(
                            "articleId", ObjectUtils.defaultIfNull(articleId, ""),
                            "commentId", ObjectUtils.defaultIfNull(commentId, "")
                    ))
                    .build();
        }

        //檢查用戶是否登入
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，無法編輯留言")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        boolean isAdmin = UserContextHolder.isCurrentUserAdmin();

        //獲取留言信息
        AmsCommentInfo commentInfo = amsCommentInfoService.getOne(
                new LambdaQueryWrapper<AmsCommentInfo>()
                        .eq(AmsCommentInfo::getCommentId, commentId)
        );

        if (commentInfo == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言信息不存在")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //檢查留言是否已被刪除（必須為未刪除狀態）
        if (commentInfo.getDeleted() != null && commentInfo.getDeleted() == 1) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.EDIT_FAILED)
                    .detailMessage("已刪除的留言無法編輯")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //驗證用戶權限：只有留言作者可以編輯
        if (!commentInfo.getUserId().equals(userId)) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.FORBIDDEN)
                    .detailMessage("無權限編輯他人留言")
                    .data(Map.of(
                            "userId", ObjectUtils.defaultIfNull(userId, ""),
                            "commentOwnerId", ObjectUtils.defaultIfNull(commentInfo.getUserId(), "")
                    ))
                    .build();
        }

        //檢查時間窗限制（管理員可繞過）
        if (!isAdmin) {
            LocalDateTime createAt = commentInfo.getCreateAt();
            LocalDateTime now = LocalDateTime.now();
            long minutesElapsed = Duration.between(createAt, now).toMinutes();

            if (minutesElapsed > editWindowMinutes) {
                throw BusinessRuntimeException.builder()
                        .iErrorCode(ResultCode.EDIT_TIME_EXPIRED)
                        .detailMessage("留言編輯時間已過期，僅可在建立後 " + editWindowMinutes + " 分鐘內編輯")
                        .data(Map.of(
                                "commentId", ObjectUtils.defaultIfNull(commentId, ""),
                                "createAt", ObjectUtils.defaultIfNull(createAt, ""),
                                "minutesElapsed", minutesElapsed,
                                "editWindowMinutes", editWindowMinutes
                        ))
                        .build();
            }
        } else {
            log.info("管理員繞過時間窗限制 - userId: {}, commentId: {}", userId, commentId);
        }

        //更新留言內容
        boolean updateSuccess = this.update(
                new LambdaUpdateWrapper<AmsComment>()
                        .eq(AmsComment::getId, commentId)
                        .set(AmsComment::getCommentContent, newContent)
        );

        if (!updateSuccess) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UPDATE_FAILED)
                    .detailMessage("更新留言內容失敗")
                    .data(Map.of("commentId", ObjectUtils.defaultIfNull(commentId, "")))
                    .build();
        }

        //更新留言信息的更新時間
        boolean updateInfoTime = amsCommentInfoService.update(
                new LambdaUpdateWrapper<AmsCommentInfo>()
                        .eq(AmsCommentInfo::getCommentId, commentId)
                        .set(AmsCommentInfo::getUpdateAt, LocalDateTime.now())
        );

        if (!updateInfoTime) {
            log.warn("更新留言信息的更新時間失敗 - commentId: {}", commentId);
        }

        log.info("留言編輯成功 - articleId: {}, commentId: {}, userId: {}", articleId, commentId, userId);

        return R.ok();
    }
}
