package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.annotation.DelayDoubleDelete;
import com.shijiawei.secretblog.article.entity.AmsArtStatus;
import com.shijiawei.secretblog.article.entity.AmsArtinfo;
import com.shijiawei.secretblog.article.entity.AmsComment;
import com.shijiawei.secretblog.article.entity.AmsCommentInfo;
import com.shijiawei.secretblog.article.feign.UserFeignClient;
import com.shijiawei.secretblog.article.mapper.AmsCommentMapper;
import com.shijiawei.secretblog.article.service.*;

import com.shijiawei.secretblog.article.vo.AmsArtCommentStaticVo;
import com.shijiawei.secretblog.article.vo.AmsArtCommentsVo;
import com.shijiawei.secretblog.article.dto.AmsCommentCreateDTO;
import com.shijiawei.secretblog.common.annotation.OpenCache;
import com.shijiawei.secretblog.common.dto.UserBasicDTO;
import com.shijiawei.secretblog.common.exception.CustomBaseException;
import com.shijiawei.secretblog.common.myenum.RedisBloomFilterKey;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.myenum.RedisLockKey;
import com.shijiawei.secretblog.common.myenum.RedisOpenCacheKey;
import com.shijiawei.secretblog.common.redisutils.RedisLuaScripts;
import com.shijiawei.secretblog.common.utils.*;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
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
//                            amsCommentInfo.setParentCommentId(amsCommentCreateDTO.getParentCommentId());
//                            //透過ParentCommentId去查詢到該父評論,並將其留言數量+1
//                            AmsCommentInfo parentAmsCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getId, amsCommentCreateDTO.getParentCommentId()));
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

    /**
     * 創建文章評論
     * @param articleId 文章ID
     * @param amsCommentCreateDTO 評論內容DTO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @DelayDoubleDelete(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX,key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY)
    @Override
    public R createComment(Long articleId,AmsCommentCreateDTO amsCommentCreateDTO) {

//        String articleBloomFilterKey = RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey();
//        RBloomFilter<Long> articleBloomFilter = redissonClient.getBloomFilter(articleBloomFilterKey);
//        if(!articleBloomFilter.contains(articleId)){
//            log.warn("文章不存在，articleId={}",articleId);
//            throw new CustomBaseException("文章不存在");
//        }

        redisBloomFilterUtils.requireExists(RedisBloomFilterKey.ARTICLE_BLOOM_FILTER.getKey(),articleId,"文章不存在");

        log.info("回覆評論 amsCommentCreateDTO:{}",amsCommentCreateDTO);
//        String jwtToken = amsCommentCreateDTO.getJwtToken();

        String userIdFromToken = null;

        try {

            if(!UserContextHolder.isCurrentUserLoggedIn()){
                log.warn("未取得登入用戶信息，拒絕新增評論");
                throw new IllegalStateException("用戶未登入，無法新增評論");
            }
            Long userId = UserContextHolder.getCurrentUserId();


            if(amsCommentCreateDTO.getParentCommentId()==null){
                return Optional.ofNullable(articleId)
                        .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
                        .filter(cmt -> !cmt.trim().isEmpty())
                        .map(cmt -> {
                            AmsComment amsComment = new AmsComment();
                            AmsCommentInfo amsCommentInfo = new AmsCommentInfo();
                            amsCommentInfo.setUserId(userId); // 使用從 Token 解析的 userId
                            amsCommentInfo.setArticleId(articleId);
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

                            //將資料庫中的文章評論數增加, 異步雙寫(DB為主、Redis為輔(可能存在與DB不一致))

                            boolean amsStatusIsUpdate = amsArtStatusService.update(
                                    new LambdaUpdateWrapper<AmsArtStatus>()
                                            .eq(AmsArtStatus::getArticleId, articleId)
                                            .setSql("comments_count = comments_count + 1")
                            );
                            if(!amsStatusIsUpdate){
                                throw new CustomBaseException("更新文章評論數失敗");
                            }
                            log.info("文章評論數更新成功，文章ID: {}", articleId);
                            // 新增：在事務提交後將 commentInfoId 放入布隆過濾器
                            redisBloomFilterUtils.saveToBloomFilterAfterCommit(
                                    commentId,
                                    RedisBloomFilterKey.COMMENT_BLOOM_FILTER.getKey()
                            );
                            //在事務提交後將 將Redis快取中的文章評論數量++
                            redisIncrementUtils.afterCommitIncrement(RedisCacheKey.ARTICLE_COMMENTS.format(articleId));


                            log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, articleId);
                            return R.ok("評論發布成功");
                        })
                        .orElseGet(() -> {
                            log.error("創建評論失敗：評論內容為空或文章ID無效");
                            return R.error("評論內容不能為空");
                        });
            }else {
                return Optional.ofNullable(articleId)
                        .flatMap(artId -> Optional.ofNullable(amsCommentCreateDTO.getCommentContent()))
                        .filter(cmt -> !cmt.trim().isEmpty())
                        .map(cmt -> {
                            AmsComment amsComment = new AmsComment();
                            AmsCommentInfo amsCommentInfo = new AmsCommentInfo();

                            long commentId = IdWorker.getId();
                            long commentInfoId = IdWorker.getId();

                            amsComment.setId(commentId);
                            amsComment.setCommentInfoId(commentInfoId);
                            amsCommentInfo.setId(commentInfoId);
                            amsCommentInfo.setCommentId(commentId);
                            amsCommentInfo.setUserId(userId); // 使用從 Token 解析的 userId
                            amsCommentInfo.setArticleId(articleId);
                            amsComment.setCommentContent(amsCommentCreateDTO.getCommentContent());
                            amsCommentInfo.setCreateAt(LocalDateTime.now());
                            amsCommentInfo.setUpdateAt(LocalDateTime.now());
                            amsCommentInfo.setParentCommentId(amsCommentCreateDTO.getParentCommentId());
                            //透過ParentCommentId去查詢到該父評論,並將其留言數量+1
                            AmsCommentInfo parentAmsCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getCommentId, amsCommentCreateDTO.getParentCommentId()));
                            parentAmsCommentInfo.setReplysCount(parentAmsCommentInfo.getReplysCount()+1);


                            this.baseMapper.insert(amsComment);
                            //儲存新創建的評論
                            amsCommentInfoService.save(amsCommentInfo);
                            //更新父評論
                            amsCommentInfoService.updateById(parentAmsCommentInfo);


                            //將資料庫中的文章評論數增加, 異步雙寫(DB為主、Redis為輔(可能存在與DB不一致))
                            AmsArtStatus amsArtStatus = amsArtStatusService
                                    .getOne(new LambdaQueryWrapper<AmsArtStatus>()
                                            .eq(AmsArtStatus::getArticleId, articleId));

                            //將資料庫中的評論數++
                            amsArtStatus.setCommentsCount(amsArtStatus.getCommentsCount()+1);
                            //寫回資料庫
                            boolean statusIsUpdate = amsArtStatusService.updateById(amsArtStatus);
                            if(!statusIsUpdate){
                                throw new CustomBaseException("更新文章評論數失敗");
                            }



                            //在事務提交後將 commentInfoId 放入布隆過濾器
                            redisBloomFilterUtils.saveToBloomFilterAfterCommit(
                                    commentId,
                                    RedisBloomFilterKey.COMMENT_BLOOM_FILTER.getKey()
                            );

                            //在事務提交後將 將Redis快取中的文章評論數量++
                            redisIncrementUtils.afterCommitIncrement(RedisCacheKey.ARTICLE_COMMENTS.format(articleId));

                            log.info("評論創建成功，用戶ID: {}, 文章ID: {}", userId, articleId);
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
            log.info("評論創建失敗，請稍後再試", e);
            return R.error("評論創建失敗，請稍後再試");
        }
    }

//    @OpenCache(prefix = "AmsComments",key = "articleId_#{#articleId}",time = 30,chronoUnit = ChronoUnit.MINUTES)
//    @Override
//    public List<AmsArtCommentsVo> getArtComments(Long articleId) {
//
//
//        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
//        if(amsArticleService.isArticleNotExists(articleId)){
//            log.info("文章不存在，articleId={}",articleId);
//            throw new CustomBaseException("文章不存在");
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
//            amsArtCommentsVo.setReplysCount(amsCommentInfo.getReplysCount());
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
     * 查詢文章中的所有評論
     * @param articleId
     * @return
     */
    @Override
    public List<AmsArtCommentsVo> getArtComments(Long articleId) {


        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
        if(amsArticleService.isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }
        //獲取文章中評論的靜態資訊
        List<AmsArtCommentStaticVo> staticCommentDetails = self.getStaticCommentDetails(articleId);
        if(staticCommentDetails.isEmpty()){
            log.info("文章無評論, articleId:{}", articleId);
            return Collections.emptyList();
        }

        /**
         * 已判斷該文章是否存在評論
         */

        log.info("留言的靜態資訊 staticCommentDetails:{}",staticCommentDetails);
        //獲取文章中評論的動態資訊(點讚數、評論數、書籤數等)
        //獲取點讚數
        RMap<Long, Long> likesCountMap = getCommentLikesCountMapFromRedis(articleId);
        log.info("留言的點讚數 likesCountMap:{}",likesCountMap);


        //將文章中評論的靜態資訊和動態資訊合併

        List<AmsArtCommentsVo> amsArtCommentsVoList = staticCommentDetails.stream().map(item -> {

            AmsArtCommentsVo amsArtCommentsVo = new AmsArtCommentsVo();

            //如果沒有對應的點讚數則給一個預設值0L
            Long commentLikeCount = likesCountMap.getOrDefault(item.getCommentId(),0L);
            amsArtCommentsVo.setLikesCount(Math.toIntExact(commentLikeCount));
            BeanUtils.copyProperties(item, amsArtCommentsVo);
            return amsArtCommentsVo;
        }).toList();
        log.info("文章評論資訊合併完成,評論內容:{}",amsArtCommentsVoList);
        return amsArtCommentsVoList;

    }


    /**
     * 查詢文章中的所有評論
     * @param articleId
     * @return
     */
    @OpenCache(prefix = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_PREFIX, key = RedisOpenCacheKey.ArticleComments.COMMENT_DETAILS_KEY, time = 30, chronoUnit = ChronoUnit.MINUTES)//
    @Override
    public List<AmsArtCommentStaticVo> getStaticCommentDetails(Long articleId) {

        //透過布隆過濾器判斷文章id是否不存在, 若不存在則拋出異常
        if(amsArticleService.isArticleNotExists(articleId)){
            log.info("文章不存在，articleId={}",articleId);
            throw new CustomBaseException("文章不存在");
        }

        //根據該文章的ArticleId查找所有關聯的CommentInfo
        List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
        if(amsCommentInfoList.isEmpty()){
            //假設文章無評論, 則直接回傳空陣列
            log.info("文章無評論, articleId:{}", articleId);
            return Collections.emptyList();
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
//        List<AmsArtCommentStaticVo> amsArtCommentsFromParentCommentId = getAmsArtCommentsFromParentCommentId(amsCommentInfo.getParentCommentId());
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


        List<AmsArtCommentStaticVo> amsArtCommentStaticVos = amsCommentInfoList.stream().map(amsCommentInfo -> {
            AmsArtCommentStaticVo amsArtCommentsStaticVo = new AmsArtCommentStaticVo();
            AmsComment amsComment = amsCommentCollect.get(amsCommentInfo.getCommentId());
            amsArtCommentsStaticVo.setCommentContent(amsComment.getCommentContent());


//            if(!userBasicDTOLongMap.isEmpty())

            if(!userBasicDTOLongMap.isEmpty()){
                UserBasicDTO userBasicDTO = userBasicDTOLongMap.get(amsCommentInfo.getUserId());
                amsArtCommentsStaticVo.setUsername(userBasicDTO.getNickName());
                amsArtCommentsStaticVo.setAvatar(userBasicDTO.getAvatar());
            }

            amsArtCommentsStaticVo.setCreateAt(amsCommentInfo.getCreateAt());
            amsArtCommentsStaticVo.setUpdateAt(amsCommentInfo.getUpdateAt());

            amsArtCommentsStaticVo.setCommentId(amsCommentInfo.getCommentId());
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
                amsArtCommentsStaticVo.setParentCommentId(artParentCommentsVo.getCommentId());

                amsArtCommentsStaticVo.setUsername("test");
                log.info("留言ID:{},父留言包裝後Vo對象:{}", amsCommentInfo.getCommentId(), artParentCommentsVo);
                return amsArtCommentsStaticVo;
            }
            return amsArtCommentsStaticVo;
        }).collect(Collectors.toList());

        log.info("amsArtCommentsVos:{}",amsArtCommentStaticVos);
        return amsArtCommentStaticVos;
    }




    /**
     * 判斷文章是否不存在，透過布隆過濾器以及資料庫雙重確認，非分佈式鎖版本
     * @param commentId
     * @return
     */
    public boolean isCommentNotExists(Long commentId) {

        if (commentId == null || commentId <= 0) {
            log.warn("非法評論ID: {}", commentId);
            return true;
        }

        /**
         * 透過布隆過濾器初步判斷該評論是否存在
         */

        try {
            /// TODO增加 Bloom 就緒旗標，透過旗標判斷是否需要檢查布隆過濾器，避免Redis服務異常導致無法使用布隆過濾器
            String commentBloomFilterPattern = RedisBloomFilterKey.COMMENT_BLOOM_FILTER.getKey();
            RBloomFilter<Long> bloomFilter = redissonClient.getBloomFilter(commentBloomFilterPattern);

            if(!bloomFilter.contains(commentId)){
                //若果布隆過濾器中不存在該評論ID，表示該評論一定不存在
                log.info("該評論ID:{}不存在或已被刪除", commentId);
                return true;
            }
        } catch (RedisConnectionException e) {
            log.error("Redis 連線異常，跳過布隆過濾器檢查，commentId={}", commentId, e);
            return existsCommentIdFromDB(commentId);

        }


        return false;

    }

//    private boolean isCommentNotExistsFromDB(Long commentId) {
//        if (commentId == null || commentId <= 0) {
//            log.warn("非法評論ID: {}", commentId);
//            return true;
//        }
//
//        AmsComment amsComment = this.baseMapper.selectById(commentId);
//        if(amsComment == null){
//            //資料庫中不存在該評論
//            log.info("該評論ID:{}不存在或已被刪除", commentId);
//            return true;
//        }
//
//        Long amsCommentId = amsComment.getId();
//
//
//        if(amsCommentId==null){
//            //表示該評論一定不存在
//            log.info("該評論ID:{}不存在或已被刪除", commentId);
//            return true;
//        }
//        return false;
//    }


//    /**
//     * 舊版本點讚
//     * @param commentId
//     * @return
//     */
//    @Transactional(rollbackFor = RuntimeException.class)
//    @Override
//    public Long OldlikeComment(Long commentId) {
//        /// TODO同步點讚數從Redis到資料庫中
//
//
//        //透過布隆過濾器判斷該評論是否不存在, 若不存在則拋出異常
//        if(this.isCommentNotExists(commentId)){
//            log.info("評論不存在，commentId={}",commentId);
//            throw new CustomBaseException("評論不存在");
//        }
//
//        /*
//          檢查用戶是否登入
//        */
//
//        if (!UserContextHolder.isCurrentUserLoggedIn()) {
//            log.warn("未取得登入用戶資訊，拒絕對評論按讚");
//            throw new CustomBaseException("未登入或登入狀態已失效");
//        }
//        Long userId = UserContextHolder.getCurrentUserId();
//        String userNameFromToken = UserContextHolder.getCurrentUserNickname();
//        if (userId == null) {
//            log.warn("用戶ID為空，拒絕對評論按讚");
//            throw new CustomBaseException("用戶ID缺失");
//        }
//
//
//
//        String BucketName = RedisCacheKey.ARTICLE_COMMENTS_LIKES.format(commentId);
//
//        log.debug("BucketName:{}",BucketName);
//        /*
//          判斷該評論是否存在
//        */
//        //獲取桶對象, 注意原子性因此採用RAtomicLong
//        RAtomicLong atomicLong = redissonClient.getAtomicLong(BucketName);
//        //先嘗試從Redis中讀取是否存在該評論的緩存
//        boolean atomicLongExists = atomicLong.isExists();
//
//
//
//        if(!atomicLongExists){
//            //若不存在則根據commentId從資料庫中讀取該評論是否存在
//            if(!this.existsCommentId(commentId)){
//                //假設從Reids緩存以及從資料庫中讀取該評論皆表明該評論不存在
//                throw new CustomBaseException("該評論不存在或已被刪除");
//            }
//
//
//        }
//
//
//        /*
//        判斷用戶是否已對該評論按讚,若已按讚則不能重複讚按
//         */
//        String userLikeKey = RedisCacheKey.COMMENT_LIKED_USERS.format(userId);
//        RSet<Long> userLikeSet = redissonClient.getSet(userLikeKey);
//
//
//
//        //成功點讚紀錄用戶已點讚
//        boolean add = userLikeSet.add(userId);
//        if(!add){
//            //若用戶已存在於點讚集合中,表示用戶已點讚過
//            log.warn("用戶ID:{}，用戶名稱:{}，重複對評論ID:{}按讚",userId,userNameFromToken,commentId);
//            throw new CustomBaseException("您已對該評論按讚過，請勿重複按讚");
//        }
//        //設置點讚值為++ , 注意原子性
//        long newLikes = atomicLong.incrementAndGet();
//        log.debug("newLikes:{}",newLikes);
//
//        return newLikes;
//    }

    /**
     * 點讚評論
     * @param articleId
     * @param commentId
     * @return
     */
    @Override
    public Long likeComment(Long articleId,Long commentId) {
        /// TODO同步點讚數從Redis到資料庫中


        //透過布隆過濾器判斷該評論是否不存在, 若不存在則拋出異常
        if(this.isCommentNotExists(commentId)){
            log.info("評論不存在，commentId={}",commentId);
            throw new CustomBaseException("評論不存在");
        }

        /*
          檢查用戶是否登入
        */

        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            log.warn("未取得登入用戶資訊，拒絕對評論按讚");
            throw new CustomBaseException("未登入或登入狀態已失效");
        }
        Long userId = UserContextHolder.getCurrentUserId();

        if (userId == null) {
            log.warn("用戶ID為空，拒絕對評論按讚");
            throw new CustomBaseException("用戶ID缺失");
        }

        String userLikedSetKey = RedisCacheKey.COMMENT_LIKED_USERS.format(commentId);
        String commentLikesHashKey = RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.format(articleId);


        /*
          判斷該評論是否存在
        */
        //獲取桶對象, 注意原子性因此採用RAtomicLong
//        RSet<Long> userLikedSet = redissonClient.getSet(userLikedSetKey);
        RMap<Long, Long> commentLikesHash = redissonClient.getMap(commentLikesHashKey);

        //先嘗試從Redis中讀取是否存在該評論的緩存
//        boolean likesHashExists = commentLikesHash.isExists();

//        if(!likesHashExists){
        //若快取中不存在該評論ID，則從資料庫中讀取該評論是否存在，若存在則新增至快取中
        if(!commentLikesHash.containsKey(commentId)){
            //若不存在則根據commentId從資料庫中讀取該評論是否存在
            Boolean commentIdFromDB = this.existsCommentIdFromDB(commentId);
            if(commentIdFromDB){

                // 從 DB 查詢評論信息（包含點讚數）
                AmsCommentInfo amsCommentInfo = amsCommentInfoService.getOne(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getCommentId, commentId));

                if(amsCommentInfo == null){

                    throw new CustomBaseException("該評論不存在或已被刪除");
                }
                // 從 DB 查詢成功得到該評論的資訊
                // 初始化緩存，將該評論的點讚數加入緩存
                commentLikesHash.put(commentId, Long.valueOf(amsCommentInfo.getLikesCount()));

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
                log.error("Lua文章中的評論點讚腳本返回null,用戶ID:{},評論ID:{}", userId, commentId);
                throw new CustomBaseException("系統異常");
            }

            if (luaResult > 0) {
                log.info("點讚成功,用戶ID:{},評論ID:{},新的按讚數:{}", userId, commentId, luaResult);
                // 返回新的按讚數
                return luaResult;
            } else if (luaResult == -1) {
                log.warn("重複點讚,用戶ID:{},評論ID:{}", userId, commentId);
                throw new CustomBaseException("您已經點過讚了");
            } else {
                log.error("Lua腳本異常返回值:{}", luaResult);
                throw new CustomBaseException("系統異常");
            }
        } catch (RedisException e) {
            log.error("Redis執行異常,用戶ID:{},評論ID:{}", userId, commentId, e);
            throw new CustomBaseException("服務暫時不可用，請稍後再試");
        }


    }

    /**
     * 從DB中判斷評論是否存在
     * @param commentId
     * @return {@code true} 如果評論存在且唯一; {@code false} 如果評論ID無效、不存在或已被刪除
     */
    @Override
    public Boolean existsCommentIdFromDB(Long commentId) {
        if (commentId == null || commentId <= 0) {
            log.warn("非法評論ID: {}", commentId);
            return false;
        }
        Long count = this.baseMapper.selectCount((new LambdaQueryWrapper<AmsComment>().eq(AmsComment::getId, commentId)));
        if(count!=1){
            log.warn("該評論ID:{}不存在或已被刪除",commentId);
            return false;
        }

        return true;

    }


//    public Long getCommentLikeCountFromRedis(Long articleId,Long commentId){
//
//
//
//        String BucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.getPattern(),articleId);
////        String BucketName = RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.format(articleId);
//
//        RMap<Long, Long> commentLikesHash = redissonClient.getMap(BucketName);
//        if(!commentLikesHash.isExists() || !commentLikesHash.containsKey(commentId)){
//            log.debug("取得文章中該則評論點讚數失敗,文章ID:{},評論ID:{}",articleId,commentId);
//            ///TODO 嘗試從DB中取得以及重建緩存
//            throw new CustomBaseException("文章中的評論點讚數無法取得,請稍後再試");
//        }
//
//
////        RAtomicLong atomicLong = redissonClient.getAtomicLong(BucketName);
////        if(!atomicLong.isExists()){
////            return 0L;
////        }
////        Long LikeCount = atomicLong.get();
//
////
////        RSet<Long> set = redissonClient.getSet(BucketName);
////        if(!set.isExists()){
////            return null;
////        }
////        set.
//
//        Long LikeCount = commentLikesHash.get(commentId);
//        if(LikeCount == null){
//            log.info("取得文章中該則評論點讚數失敗,文章ID:{},評論ID:{}",articleId,commentId);
//            return 0L;
//        }
//        log.info("LikeCount:{}",LikeCount);
//        return LikeCount;
//    }

//    public RMap<Long, Long> getCommentLikesCountMapFromRedis(Long articleId){
//
//
//
//        String bucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.getPattern(),articleId);
////        String BucketName = RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.format(articleId);
//
//        RMap<Long, Long> commentLikesHash = redissonClient.getMap(bucketName,new JsonJacksonCodec());
//        if(!commentLikesHash.isExists()){
//            log.debug("取得文章中所有評論點讚數失敗,文章ID:{}",articleId);
//            ///TODO 嘗試從DB中取得以及重建緩存
//            String lockKey = String.format(RedisLockKey.ARTICLE_COMMENTS_EXISTS_LOCK.getPattern(), articleId);
//            RLock lock = redissonClient.getLock(lockKey);
//            boolean locked = false;
//            try {
//                locked = lock.tryLock(3, 10, TimeUnit.SECONDS);
//                if(!locked){
//                    //未成功取得鎖(鎖可能正被其他線程占用)
//                    //持續檢查最多60秒
//                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
//                    while(System.nanoTime() < deadline){
//
//                        commentLikesHash = redissonClient.getMap(bucketName);
//                        if(commentLikesHash.isExists()){
//                            //已由其他線程初始化完畢
//                            return commentLikesHash;
//                        }
//
//                        //間隔時間
//                        TimeUnit.SECONDS.sleep(2);
//                    }
//
//                    throw new CustomBaseException("文章中的評論點讚數無法取得,請稍後再試");
//                } else {
//                    //已成功取得鎖
//                    //從資料庫中取得數據前再次判斷是否已存在
//                    if(commentLikesHash.isExists()) return commentLikesHash;
//
//                    //從資料庫中取得該文章的評論點讚數
//                    List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
//                    if(amsCommentInfoList.isEmpty()){
//                        /*
//                        在進入這個區塊之前已經判斷過文章是否存在評論
//                        所以在這裡就不再判斷是否存在評論了
//                        假設文章無評論,則拋出異常
//                         */
//                        throw new CustomBaseException("文章無評論, 請稍後再試");
//
//                    }
//
//                    Map<@NotNull Long, Long> commentLikesHashFromDB = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getCommentId,comment -> comment.getLikesCount().longValue()));
//                    //再將該文章的評論點讚數加入至緩存中之前再次判斷是否已存在, 防止其他線程在取得鎖後已存在 , 若已存在則直接回傳
//                    //將從資料庫中取得的資料加入至緩存中
//                    commentLikesHash.putAll(commentLikesHashFromDB);
//                    return commentLikesHash;
//
//                }
//
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//                log.error("文章中的評論點讚數無法取得,請稍後再試,異常錯誤:"+e.getMessage());
//                throw new CustomBaseException("文章中的評論點讚數無法取得,請稍後再試,異常錯誤:"+e.getMessage());
//            } finally {
//                if( locked && lock.isHeldByCurrentThread()){
//                    lock.unlock();
//                }
//
//            }
//        }
//
//
//        log.info("commentLikesHash:{}",commentLikesHash);
//        return commentLikesHash;
//    }

    /**
     * 取得文章中評論的點讚數聚合（Hash: field=commentId, value=likesCount）
     * 先從Redis中取得，如果不存在則從DB中取得，再將DB中的資料加入Redis中進行快取
     * @param articleId
     * @return
     */
    public RMap<Long, Long> getCommentLikesCountMapFromRedis(Long articleId){



        String bucketName = String.format(RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.getPattern(),articleId);
//        String BucketName = RedisCacheKey.ARTICLE_COMMENT_LIKES_HASH.format(articleId);

        RMap<Long, Long> commentLikesHash = redissonClient.getMap(bucketName,new TypedJsonJacksonCodec(Long.class,
                Long.class));
        if(!commentLikesHash.isExists()){
            log.debug("取得文章中所有評論點讚數失敗,文章ID:{}",articleId);
            ///TODO 嘗試從DB中取得以及重建緩存
            String lockKey = String.format(RedisLockKey.ARTICLE_COMMENTS_EXISTS_LOCK.getPattern(), articleId);
            RLock lock = redissonClient.getLock(lockKey);
            boolean locked = false;
            try {
                locked = lock.tryLock(2, 10, TimeUnit.SECONDS);
                if(!locked){
                    //未成功取得鎖(鎖可能正被其他線程占用)
                    //持續檢查最多60秒
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(8);
                    while(System.nanoTime() < deadline){

                        commentLikesHash = redissonClient.getMap(bucketName,new TypedJsonJacksonCodec(Long.class,
                                Long.class));
                        if(commentLikesHash.isExists()){
                            //已由其他線程初始化完畢
                            return commentLikesHash;
                        }

                        //間隔時間
                        TimeUnit.SECONDS.sleep(2);
                    }

                    throw new CustomBaseException("文章中的評論點讚數無法取得,請稍後再試");
                } else {
                    //已成功取得鎖
                    //從資料庫中取得數據前再次判斷是否已存在
                    if(commentLikesHash.isExists()) return commentLikesHash;

                    Map<Long, Long> commentLikesCountMapFromDB = getCommentLikesCountMapFromDB(articleId);

                    //將從資料庫中取得的資料加入至緩存中
                    commentLikesHash.putAll(commentLikesCountMapFromDB);

                    log.info("commentLikesCountMapFromDB:{}",commentLikesCountMapFromDB);

//                    //已成功取得鎖
//                    //從資料庫中取得數據前再次判斷是否已存在
//                    if(commentLikesHash.isExists()) return commentLikesHash;
//
//                    //從資料庫中取得該文章的評論點讚數
//                    List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
//                    if(amsCommentInfoList.isEmpty()){
//                        /*
//                        在進入這個區塊之前已經判斷過文章是否存在評論
//                        所以在這裡就不再判斷是否存在評論了
//                        假設文章無評論,則拋出異常
//                         */
//                        throw new CustomBaseException("文章無評論, 請稍後再試");
//
//                    }
//
//                    Map<@NotNull Long, Long> commentLikesHashFromDB = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getCommentId,comment -> comment.getLikesCount().longValue()));
//
//
//
//                    //再將該文章的評論點讚數加入至緩存中之前再次判斷是否已存在, 防止其他線程在取得鎖後已存在 , 若已存在則直接回傳
//                    //將從資料庫中取得的資料加入至緩存中
//                    commentLikesHash.putAll(commentLikesHashFromDB);
//                    return commentLikesHash;

                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("文章中的評論點讚數無法取得,請稍後再試,異常錯誤:"+e.getMessage());
                throw new CustomBaseException("文章中的評論點讚數無法取得,請稍後再試,異常錯誤:"+e.getMessage());
            } finally {
                if( locked && lock.isHeldByCurrentThread()){
                    lock.unlock();
                }

            }
        }


        log.info("commentLikesHash:{}",commentLikesHash);
        return commentLikesHash;
    }

    public Map<Long, Long> getCommentLikesCountMapFromDB(Long articleId){

        //從資料庫中取得該文章的評論點讚數
        List<AmsCommentInfo> amsCommentInfoList = amsCommentInfoService.list(new LambdaQueryWrapper<AmsCommentInfo>().eq(AmsCommentInfo::getArticleId, articleId));
        if(amsCommentInfoList.isEmpty()){
            /*
            在進入這個區塊之前已經判斷過文章是否存在評論
            所以在這裡就不再判斷是否存在評論了
            假設文章無評論,則拋出異常
             */
            throw new CustomBaseException("文章無評論, 請稍後再試");

        }

        Map<@NotNull Long, Long> commentLikesHashFromDB = amsCommentInfoList.stream().collect(Collectors.toMap(AmsCommentInfo::getCommentId,comment -> comment.getLikesCount().longValue()));
        //再將該文章的評論點讚數加入至緩存中之前再次判斷是否已存在, 防止其他線程在取得鎖後已存在 , 若已存在則直接回傳


        log.info("commentLikesHashFromDB:{}",commentLikesHashFromDB);
        return commentLikesHashFromDB;
    }


}

