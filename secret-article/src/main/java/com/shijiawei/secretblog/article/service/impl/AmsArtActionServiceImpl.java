package com.shijiawei.secretblog.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.article.entity.AmsArtAction;
import com.shijiawei.secretblog.article.mapper.AmsArtActionMapper;
import com.shijiawei.secretblog.article.service.AmsArtActionService;
import com.shijiawei.secretblog.article.service.AmsArticleService;
import com.shijiawei.secretblog.article.vo.AmsArtActionVo;
import com.shijiawei.secretblog.article.vo.UserLikedArticleVo;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RFuture;
import org.redisson.api.RSetAsync;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisConnectionException;
import org.redisson.client.RedisTimeoutException;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * ClassName: AmsArtActionServiceImpl
 * Description:
 *
 * @Create 2025/11/25 下午10:10
 */
@Slf4j
@Service
public class AmsArtActionServiceImpl extends ServiceImpl<AmsArtActionMapper, AmsArtAction> implements AmsArtActionService {


    private static final byte NO_INTERACTION_SENTINEL = -1;
    private static final byte INTERACTION_FALSE = 0;
    private static final byte INTERACTION_TRUE = 1;

    @Autowired
    @Lazy
    private AmsArticleService amsArticleService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public AmsArtActionVo getArticleActionStatusVo(Long articleId) {

        //先檢查文章ID是否存在
        amsArticleService.isArticleNotExists(articleId);


        //檢查用戶是否成功登入
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

        //嘗試從Redis快取中取得資料
        Boolean isLikedFromRedis = null;
        Boolean isBookmarkedFromRedis = null;
        boolean redisSuccess = false;

        final String userIdStr = String.valueOf(userId);
        final String likedUsersKey = RedisCacheKey.ARTICLE_LIKED_USERS.format(articleId);
        final String bookmarkedUsersKey = RedisCacheKey.ARTICLE_MARKED_USERS.format(articleId);

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

            //判斷集合是否存在，避免快取尚未初始化，導致誤判斷用戶互動狀態，
            //假設尚未初始化則放行讓其查詢資料庫並初始化快取
            if(likedKeyExists && bookmarkedKeyExists){
                // 若兩個集合都存在，則表示用戶互動狀態已被快取
                redisSuccess = true;

            }

            log.info("成功從 Redis 批次查詢文章互動狀態 - articleId: {}, userId: {}, isLiked: {}, isBookmarked: {}",
                    articleId, userId, isLikedFromRedis, isBookmarkedFromRedis);

        } catch (RedisConnectionException | RedisTimeoutException e) {
            log.warn("Redis 連接/超時異常，降級至資料庫查詢 - articleId: {}, userId: {}, error: {}",
                    articleId, userId, e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Redis 批次查詢被中斷，降級至資料庫查詢 - articleId: {}, userId: {}",
                    articleId, userId);
        } catch (ExecutionException e) {
            log.warn("Redis 批次查詢執行失敗，降級至資料庫查詢 - articleId: {}, userId: {}, error: {}",
                    articleId, userId, e.getMessage());
        } catch (Exception e) {
            log.warn("Redis 查詢未預期異常，降級至資料庫查詢 - articleId: {}, userId: {}, error: {}",
                    articleId, userId, e.getMessage());
        }

        /**
         * Redis快取中取得資料失敗，從資料庫中取得
         */
        Byte isLiked;
        Byte isBookmarked;

        if (!redisSuccess) {
            log.info("開始從資料庫查詢文章互動狀態 - articleId: {}, userId: {}", articleId, userId);

            LambdaQueryWrapper<AmsArtAction> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AmsArtAction::getArticleId, articleId)
                    .eq(AmsArtAction::getUserId, userId);

            AmsArtAction actionRecord = this.getOne(queryWrapper);

            if (actionRecord != null) {
                isLiked = actionRecord.getIsLiked();
                isBookmarked = actionRecord.getIsBookmarked();
                log.info("從資料庫查詢到文章互動狀態 - articleId: {}, userId: {}, isLiked: {}, isBookmarked: {}",
                        articleId, userId, isLiked, isBookmarked);
            } else {
                // 用戶未互動過，使用 -1 標記（用於快取穿透防護）
                isLiked = NO_INTERACTION_SENTINEL;
                isBookmarked = NO_INTERACTION_SENTINEL;
                log.info("資料庫未找到互動記錄，使用 -1 標記快取穿透 - articleId: {}, userId: {}", articleId, userId);
            }
            warmupCacheAfterDbQuery(articleId, userIdStr, likedUsersKey, bookmarkedUsersKey, isLiked, isBookmarked);
        } else {
            isLiked = (isLikedFromRedis != null && isLikedFromRedis) ? INTERACTION_TRUE : INTERACTION_FALSE;
            isBookmarked = (isBookmarkedFromRedis != null && isBookmarkedFromRedis) ? INTERACTION_TRUE : INTERACTION_FALSE;
        }

        Byte finalIsLiked = (isLiked == NO_INTERACTION_SENTINEL) ? INTERACTION_FALSE : isLiked;
        Byte finalIsBookmarked = (isBookmarked == NO_INTERACTION_SENTINEL) ? INTERACTION_FALSE : isBookmarked;

        // 構建並返回 AmsArtAction 物件
        return buildAction(finalIsLiked, finalIsBookmarked);
    }

    private AmsArtActionVo buildAction(Byte isLiked, Byte isBookmarked) {
        return AmsArtActionVo.builder()
                .isLiked(isLiked)
                .isBookmarked(isBookmarked)
                .build();
    }

    /**
     * 資料庫查詢後預熱快取，防止快取穿透
     *
     * @param articleId     文章ID
     * @param userIdStr     用戶ID 字串
     * @param likedUsersKey 點讚用戶的 Redis 鍵
     * @param bookmarkedUsersKey 書籤用戶的 Redis 鍵
     * @param isLiked       是否點讚 (0 或 1)
     * @param isBookmarked  是否書籤 (0 或 1)
     */
    private void warmupCacheAfterDbQuery(Long articleId, String userIdStr, String likedUsersKey, String bookmarkedUsersKey, Byte isLiked, Byte isBookmarked) {
        try {
            // 判斷是否有實際互動數據（點讚或書籤為1，-1表示無互動數據）
            boolean hasActualData = (isLiked != null && isLiked == INTERACTION_TRUE) || (isBookmarked != null && isBookmarked == INTERACTION_TRUE);

            // 根據是否有數據設置不同的 TTL
            Duration ttl;
            if (hasActualData) {
                // 有實際數據，使用 ARTICLE_LIKED_USERS/ARTICLE_MARKED_USERS 的 TTL (30分鐘)
                ttl = RedisCacheKey.ARTICLE_LIKED_USERS.getTtl() != null
                    ? RedisCacheKey.ARTICLE_LIKED_USERS.getTtl()
                    : Duration.ofMinutes(30); // 預設30分鐘
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

            log.info("快取預熱完成 - articleId: {}, userId: {}, isLiked(raw): {}, isBookmarked(raw): {}, hasActualData: {}, TTL(min): {}",
                    articleId, userIdStr, isLiked, isBookmarked, hasActualData, ttl.toMinutes());
        } catch (Exception e) {
            // 快取預熱失敗不影響業務邏輯，僅記錄日誌
            log.warn("快取預熱失敗（忽略） - articleId: {}, userId: {}, error: {}",
                    articleId, userIdStr, e.getMessage());
        }
    }

    @Override
    public List<UserLikedArticleVo> getLikedArticlesByUserId(Long userId) {
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.NOT_FOUND)
                    .detailMessage("查詢的目標用戶ID不存在")
                    .build();
        }

        log.info("查詢用戶點讚文章列表 - userId: {}", userId);
        return baseMapper.selectLikedArticlesByUserId(userId);
    }
}
