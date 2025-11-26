package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsCommentAction;
import com.shijiawei.secretblog.article.entity.AmsCommentInfo;
import com.shijiawei.secretblog.article.mapper.AmsCommentActionMapper;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.service.AmsCommentActionService;
import com.shijiawei.secretblog.article.service.AmsCommentInfoService;
import com.shijiawei.secretblog.article.vo.AmsCommentActionVo;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.*;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.shijiawei.secretblog.common.myenum.RedisCacheKey.COMMENT_LIKED_USERS;

/**
 * ClassName: AmsCommentActionServiceImpl
 * Description: 留言互動記錄 Service 實作
 *
 * @Create 2025/11/26
 */
@Slf4j
@Service
public class AmsCommentActionServiceImpl extends ServiceImpl<AmsCommentActionMapper, AmsCommentAction> implements AmsCommentActionService {

    private static final byte NO_INTERACTION_SENTINEL = -1;
    private static final byte INTERACTION_FALSE = 0;
    private static final byte INTERACTION_TRUE = 1;

    @Autowired
    @Lazy
    private AmsCommentInfoService amsCommentInfoService;

    @Autowired
    @Lazy
    private AmsArticleService amsArticleService;

    @Autowired
    private RedissonClient redissonClient;




    @Override
    public AmsCommentActionVo getCommentActionStatusVo(Long commentId) {

        // 先檢查留言ID是否存在
        isCommentNotExists(commentId);

        // 檢查用戶是否成功登入
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶ID不存在")
                    .build();
        }

        // 嘗試從Redis快取中取得資料
        Boolean isLikedFromRedis = null;
        Boolean isBookmarkedFromRedis = null;
        boolean redisSuccess = false;

        final String userIdStr = String.valueOf(userId);
        final String likedUsersKey = COMMENT_LIKED_USERS.format(commentId);
        final String bookmarkedUsersKey = RedisCacheKey.COMMENT_MARKED_USERS.format(commentId);

        try {
            RBatch batch = redissonClient.createBatch();

            RSetAsync<String> likedUsersSetAsync = batch.getSet(likedUsersKey, StringCodec.INSTANCE);
            RSetAsync<String> bookmarkedUsersSetAsync = batch.getSet(bookmarkedUsersKey, StringCodec.INSTANCE);
            RFuture<Boolean> likedFuture = likedUsersSetAsync.containsAsync(userIdStr);
            RFuture<Boolean> bookmarkedFuture = bookmarkedUsersSetAsync.containsAsync(userIdStr);

            RFuture<Boolean> likedKeyExistsFuture = batch.getSet(likedUsersKey).isExistsAsync();
            RFuture<Boolean> bookmarkedKeyExistsFuture = batch.getSet(bookmarkedUsersKey).isExistsAsync();
            // 執行批次操作
            batch.execute();

            boolean likedKeyExists = likedKeyExistsFuture.get();
            boolean bookmarkedKeyExists = bookmarkedKeyExistsFuture.get();
            // 獲取結果
            isLikedFromRedis = likedFuture.get();
            isBookmarkedFromRedis = bookmarkedFuture.get();

            // 判斷集合是否存在，避免快取尚未初始化，導致誤判斷用戶互動狀態
            if (likedKeyExists && bookmarkedKeyExists) {
                redisSuccess = true;
            }

            log.info("成功從 Redis 批次查詢留言互動狀態 - commentId: {}, userId: {}, isLiked: {}, isBookmarked: {}",
                    commentId, userId, isLikedFromRedis, isBookmarkedFromRedis);

        } catch (RedisConnectionException | RedisTimeoutException e) {
            log.warn("Redis 連接/超時異常，降級至資料庫查詢 - commentId: {}, userId: {}, error: {}",
                    commentId, userId, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Redis 批次查詢被中斷，降級至資料庫查詢 - commentId: {}, userId: {}",
                    commentId, userId);
        } catch (ExecutionException e) {
            log.warn("Redis 批次查詢執行失敗，降級至資料庫查詢 - commentId: {}, userId: {}, error: {}",
                    commentId, userId, e.getMessage());
        } catch (Exception e) {
            log.warn("Redis 查詢未預期異常，降級至資料庫查詢 - commentId: {}, userId: {}, error: {}",
                    commentId, userId, e.getMessage());
        }

        /**
         * Redis快取中取得資料失敗，從資料庫中取得
         */
        Byte isLiked;
        Byte isBookmarked;

        if (!redisSuccess) {
            log.info("開始從資料庫查詢留言互動狀態 - commentId: {}, userId: {}", commentId, userId);

            LambdaQueryWrapper<AmsCommentAction> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AmsCommentAction::getCommentId, commentId)
                    .eq(AmsCommentAction::getUserId, userId);

            AmsCommentAction actionRecord = this.getOne(queryWrapper);

            if (actionRecord != null) {
                isLiked = actionRecord.getIsLiked();
                isBookmarked = actionRecord.getIsBookmarked();
                log.info("從資料庫查詢到留言互動狀態 - commentId: {}, userId: {}, isLiked: {}, isBookmarked: {}",
                        commentId, userId, isLiked, isBookmarked);
            } else {
                // 用戶未互動過，使用 -1 標記（用於快取穿透防護）
                isLiked = NO_INTERACTION_SENTINEL;
                isBookmarked = NO_INTERACTION_SENTINEL;
                log.info("資料庫未找到互動記錄，使用 -1 標記快取穿透 - commentId: {}, userId: {}", commentId, userId);
            }
            warmupCacheAfterDbQuery(commentId, userIdStr, likedUsersKey, bookmarkedUsersKey, isLiked, isBookmarked);
        } else {
            isLiked = (isLikedFromRedis != null && isLikedFromRedis) ? INTERACTION_TRUE : INTERACTION_FALSE;
            isBookmarked = (isBookmarkedFromRedis != null && isBookmarkedFromRedis) ? INTERACTION_TRUE : INTERACTION_FALSE;
        }

        Byte finalIsLiked = (isLiked == NO_INTERACTION_SENTINEL) ? INTERACTION_FALSE : isLiked;
        Byte finalIsBookmarked = (isBookmarked == NO_INTERACTION_SENTINEL) ? INTERACTION_FALSE : isBookmarked;

        // 構建並返回 AmsCommentActionVo 物件
        return buildAction(finalIsLiked, finalIsBookmarked);
    }

    @Override
    public List<AmsCommentActionVo> getCommentActionStatusVos(Long articleId) {

        // 先檢查留言ID是否存在
        amsArticleService.isArticleNotExists(articleId);

        // 檢查用戶是否成功登入
        if (!UserContextHolder.isCurrentUserLoggedIn()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入")
                    .build();
        }
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("用戶ID不存在")
                    .build();
        }

        final String userIdStr = String.valueOf(userId);

        String likesCountPattern = RedisCacheKey.ARTICLE_COMMENT_LIKES_COUNT_HASH.getPattern();
        String likedUsersKeyPattern = RedisCacheKey.COMMENT_LIKED_USERS.getPattern();
        String bookmarkedUsersKeyPattern = RedisCacheKey.COMMENT_MARKED_USERS.getPattern();

        final String likesCountBucketName = String.format(likesCountPattern, articleId);

        try {
            // 獲取所有留言ID
            RMap<Long, Integer> likesMap = redissonClient.getMap(likesCountBucketName, new TypedJsonJacksonCodec(Long.class, Integer.class));
            Set<Long> commentIds = likesMap.keySet();

            if (commentIds.isEmpty()) {
                log.info("文章下無留言 - articleId: {}", articleId);
                return List.of();
            }

            // 生成所有需要查詢的 Redis Key
            List<String> likedUsersKeyList = commentIds.stream()
                    .map(RedisCacheKey.COMMENT_LIKED_USERS::format)
                    .toList();

            List<String> bookmarkedUsersKeyList = commentIds.stream()
                    .map(RedisCacheKey.COMMENT_MARKED_USERS::format)
                    .toList();

            // 批次查詢
            RBatch batch = redissonClient.createBatch();

            Map<String, CompletableFuture<Boolean>> likedFutureMap = likedUsersKeyList.stream()
                    .collect(Collectors.toMap(
                            likedUsersKey -> likedUsersKey,
                            likedUsersKey -> {
                                RSetAsync<Object> set = batch.getSet(likedUsersKey, StringCodec.INSTANCE);
                                RFuture<Boolean> future = set.containsAsync(userIdStr);
                                return future.toCompletableFuture();
                            }
                    ));

            Map<String, CompletableFuture<Boolean>> bookmarkedFutureMap = bookmarkedUsersKeyList.stream()
                    .collect(Collectors.toMap(
                            bookmarkedUsersKey -> bookmarkedUsersKey,
                            bookmarkedUsersKey -> {
                                RSetAsync<Object> set = batch.getSet(bookmarkedUsersKey, StringCodec.INSTANCE);
                                RFuture<Boolean> future = set.containsAsync(userIdStr);
                                return future.toCompletableFuture();
                            }
                    ));

            //            likedUsersSetAsync.containsAllAsync()
//
//
//            RFuture<Boolean> likedFuture = likedUsersSetAsync.containsAsync(userIdStr);
//            RFuture<Boolean> bookmarkedFuture = bookmarkedUsersSetAsync.containsAsync(userIdStr);
//
//            RFuture<Boolean> likedKeyExistsFuture = batch.getSet(likedUsersKey).isExistsAsync();
//            RFuture<Boolean> bookmarkedKeyExistsFuture = batch.getSet(bookmarkedUsersKey).isExistsAsync();
            // 執行批次操作
            batch.execute();

            // 同步等待所有結果
            CompletableFuture.allOf(likedFutureMap.values().toArray(new CompletableFuture[0])).join();
            CompletableFuture.allOf(bookmarkedFutureMap.values().toArray(new CompletableFuture[0])).join();

            // 收集點讚結果
            Map<Long, Boolean> likedResultMap = likedFutureMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> extractCommentIdFromKey(entry.getKey(), likedUsersKeyPattern),
                            entry -> entry.getValue().join()
                    ));

            // 收集書籤結果
            Map<Long, Boolean> bookmarkedResultMap = bookmarkedFutureMap.entrySet().stream()
                    .collect(Collectors.toMap(
                            entry -> extractCommentIdFromKey(entry.getKey(), bookmarkedUsersKeyPattern),
                            entry -> entry.getValue().join()
                    ));

//            isLikedFromRedis = likedFuture.get();
//            isBookmarkedFromRedis = bookmarkedFuture.get();
//
//            // 判斷集合是否存在，避免快取尚未初始化，導致誤判斷用戶互動狀態
//            if (likedKeyExists && bookmarkedKeyExists) {
//                redisSuccess = true;
//            }
            log.info("成功從 Redis 批次查詢留言互動狀態 - articleId: {}, userId: {}",
                    articleId, userId);
            // 構建返回結果列表
            return commentIds.stream()
                    .map(commentId -> AmsCommentActionVo.builder()
                            .articleId(articleId)
                            .commentId(commentId)
                            .isLiked(likedResultMap.getOrDefault(commentId, false) ? INTERACTION_TRUE : INTERACTION_FALSE)
                            .isBookmarked(bookmarkedResultMap.getOrDefault(commentId, false) ? INTERACTION_TRUE : INTERACTION_FALSE)
                            .build())
                    .collect(Collectors.toList());

        } catch (RedisConnectionException | RedisTimeoutException e) {
            log.warn("Redis 連接/超時異常，降級至資料庫查詢 - articleId: {}, userId: {}, error: {}",
                    articleId, userId, e.getMessage());
        } catch (Exception e) {
            log.warn("Redis 查詢未預期異常，降級至資料庫查詢 - articleId: {}, userId: {}, error: {}",
                    articleId, userId, e.getMessage());
        }

        // 降級：從資料庫查詢
        return getCommentActionStatusVosFromDb(articleId, userId);
    }

    /**
     * 從 Redis Key 中提取留言ID
     *
     * @param key     Redis Key
     * @param pattern Key 模式
     * @return 留言ID
     */
    private Long extractCommentIdFromKey(String key, String pattern) {
        String regex = pattern.replace("%s", "(\\d+)");
        Pattern compile = Pattern.compile(regex);
        Matcher matcher = compile.matcher(key);

        if (!matcher.find()) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_ERROR)
                    .detailMessage("從Redis Key 中抓取值時格式錯誤: " + key)
                    .build();
        }
        return Long.parseLong(matcher.group(1));
    }

    /**
     * 從資料庫查詢文章下所有留言的互動狀態（降級方案）
     *
     * @param articleId 文章ID
     * @param userId    用戶ID
     * @return 留言互動狀態 VO 列表
     */
    private List<AmsCommentActionVo> getCommentActionStatusVosFromDb(Long articleId, Long userId) {
        log.info("開始從資料庫查詢文章留言互動狀態 - articleId: {}, userId: {}", articleId, userId);

        // 獲取文章下所有留言
        LambdaQueryWrapper<AmsCommentInfo> commentQueryWrapper = new LambdaQueryWrapper<>();
        commentQueryWrapper.eq(AmsCommentInfo::getArticleId, articleId)
                .select(AmsCommentInfo::getId);
        List<AmsCommentInfo> comments = amsCommentInfoService.list(commentQueryWrapper);

        if (comments.isEmpty()) {
            log.info("資料庫查詢：文章下無留言 - articleId: {}", articleId);
            return List.of();
        }

        List<Long> commentIds = comments.stream()
                .map(AmsCommentInfo::getId)
                .toList();

        // 查詢用戶對這些留言的互動記錄
        LambdaQueryWrapper<AmsCommentAction> actionQueryWrapper = new LambdaQueryWrapper<>();
        actionQueryWrapper.eq(AmsCommentAction::getUserId, userId)
                .in(AmsCommentAction::getCommentId, commentIds);
        List<AmsCommentAction> actions = this.list(actionQueryWrapper);

        // 構建互動記錄 Map
        Map<Long, AmsCommentAction> actionMap = actions.stream()
                .collect(Collectors.toMap(AmsCommentAction::getCommentId, action -> action));

        log.info("從資料庫查詢到留言互動狀態 - articleId: {}, userId: {}, commentCount: {}, actionCount: {}",
                articleId, userId, commentIds.size(), actions.size());

        // 構建返回結果
        return commentIds.stream()
                .map(commentId -> {
                    AmsCommentAction action = actionMap.get(commentId);
                    return AmsCommentActionVo.builder()
                            .articleId(articleId)
                            .commentId(commentId)
                            .isLiked(action != null && action.getIsLiked() == INTERACTION_TRUE ? INTERACTION_TRUE : INTERACTION_FALSE)
                            .isBookmarked(action != null && action.getIsBookmarked() == INTERACTION_TRUE ? INTERACTION_TRUE : INTERACTION_FALSE)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 檢查留言是否存在
     *
     * @param commentId 留言ID
     */
    private void isCommentNotExists(Long commentId) {
        if (commentId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.PARAM_MISSING)
                    .detailMessage("留言ID不能為空")
                    .build();
        }

        AmsCommentInfo commentInfo = amsCommentInfoService.getById(commentId);
        if (commentInfo == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("留言不存在")
                    .build();
        }
    }

    private AmsCommentActionVo buildAction(Byte isLiked, Byte isBookmarked) {
        return AmsCommentActionVo.builder()
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .build();
    }

    /**
     * 資料庫查詢後預熱快取，防止快取穿透
     *
     * @param commentId          留言ID
     * @param userIdStr          用戶ID 字串
     * @param likedUsersKey      點讚用戶的 Redis 鍵
     * @param bookmarkedUsersKey 書籤用戶的 Redis 鍵
     * @param isLiked            是否點讚 (0 或 1)
     * @param isBookmarked       是否書籤 (0 或 1)
     */
    private void warmupCacheAfterDbQuery(Long commentId, String userIdStr, String likedUsersKey, String bookmarkedUsersKey, Byte isLiked, Byte isBookmarked) {
        try {
            // 判斷是否有實際互動數據（點讚或書籤為1，-1表示無互動數據）
            boolean hasActualData = (isLiked != null && isLiked == INTERACTION_TRUE) || (isBookmarked != null && isBookmarked == INTERACTION_TRUE);

            // 根據是否有數據設置不同的 TTL
            Duration ttl;
            if (hasActualData) {
                // 有實際數據，使用 30 分鐘 TTL
                ttl = Duration.ofMinutes(30);
            } else {
                // 無數據（防止快取穿透），設置較短的 TTL (3分鐘)
                ttl = Duration.ofMinutes(3);
            }

            RBatch batch = redissonClient.createBatch();

            // 根據資料庫查詢結果更新 Redis Set
            if (isLiked != null && isLiked == INTERACTION_TRUE) {
                // 用戶已點讚，加入點讚集合
                batch.getSet(likedUsersKey, StringCodec.INSTANCE).addAsync(userIdStr);
            } else {
                // 用戶未點讚，確保不在點讚集合中（移除，若存在）
                batch.getSet(likedUsersKey, StringCodec.INSTANCE).removeAsync(userIdStr);
            }

            if (isBookmarked != null && isBookmarked == INTERACTION_TRUE) {
                // 用戶已書籤，加入書籤集合
                batch.getSet(bookmarkedUsersKey, StringCodec.INSTANCE).addAsync(userIdStr);
            } else {
                // 用戶未書籤，確保不在書籤集合中（移除，若存在）
                batch.getSet(bookmarkedUsersKey, StringCodec.INSTANCE).removeAsync(userIdStr);
            }

            // 執行批次操作
            batch.execute();

            // 設置 TTL（點讚和書籤集合使用相同的 TTL）
            redissonClient.getSet(likedUsersKey, StringCodec.INSTANCE).expire(ttl);
            redissonClient.getSet(bookmarkedUsersKey, StringCodec.INSTANCE).expire(ttl);

            log.info("快取預熱完成 - commentId: {}, userId: {}, isLiked(raw): {}, isBookmarked(raw): {}, hasActualData: {}, TTL(min): {}",
                    commentId, userIdStr, isLiked, isBookmarked, hasActualData, ttl.toMinutes());
        } catch (Exception e) {
            // 快取預熱失敗不影響業務邏輯，僅記錄日誌
            log.warn("快取預熱失敗（忽略） - commentId: {}, userId: {}, error: {}",
                    commentId, userIdStr, e.getMessage());
        }
    }
}
