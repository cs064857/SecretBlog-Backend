package com.shijiawei.secretblog.user.service.Impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.shijiawei.secretblog.common.myenum.RedisCacheKey;
import com.shijiawei.secretblog.user.entity.UmsUserInbox;
import com.shijiawei.secretblog.user.mapper.UmsUserInboxMapper;
import com.shijiawei.secretblog.user.service.UmsUserInboxService;
import com.shijiawei.secretblog.common.utils.AvatarUrlHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 使用者通知收件匣
 */
@Service
@Slf4j
public class UmsUserInboxServiceImpl extends ServiceImpl<UmsUserInboxMapper, UmsUserInbox>
        implements UmsUserInboxService {

    @Autowired
    private RedissonClient redissonClient;

    @Value("${custom.minio-domain}")
    private String minioDomain;

    @Override
    public Page<UmsUserInbox> getUserInboxPage(Long userId, Integer routePage, Boolean onlyUnread) {

        //每頁20筆
        int pageSize = 20;

        long current = routePage == null || routePage < 1 ? 1L : routePage.longValue();
        Page<UmsUserInbox> page = new Page<>(current, pageSize);

        //優先從 Redis 快取讀取(UmsLocalMessageConsumer 會同步寫入 USER_INBOX)
        try {
            String inboxKey = RedisCacheKey.USER_INBOX.format(userId);
            RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
            List<UmsUserInbox> cachedList = rList.readAll();
            if (cachedList != null && !cachedList.isEmpty()) {
                List<UmsUserInbox> filtered = new ArrayList<>(cachedList.size());
                for (UmsUserInbox inbox : cachedList) {
                    if (inbox == null) {
                        continue;
                    }
                    if (inbox.getDeleted() != null && inbox.getDeleted() != 0) {
                        continue;
                    }
                    if (Boolean.TRUE.equals(onlyUnread) && (inbox.getReadFlag() == null || inbox.getReadFlag() != 0)) {
                        continue;
                    }
                    filtered.add(inbox);
                }

                //快取清單為追加寫入(舊 -> 新)，回傳時需轉為最新優先
                Collections.reverse(filtered);

                page.setTotal(filtered.size());
                int fromIndex = (int) ((current - 1) * pageSize);
                if (fromIndex >= filtered.size()) {
                    page.setRecords(Collections.emptyList());
                    return page;
                }
                int toIndex = Math.min(fromIndex + pageSize, filtered.size());
                List<UmsUserInbox> pageRecords = filtered.subList(fromIndex, toIndex);
                for (UmsUserInbox inbox : pageRecords) {
                    if (inbox == null) {
                        continue;
                    }
                    inbox.setFromAvatar(AvatarUrlHelper.toPublicUrl(inbox.getFromAvatar(), minioDomain));
                }
                page.setRecords(pageRecords);
                return page;
            }
        } catch (Exception e) {
            log.warn("讀取使用者收件匣快取失敗，將改由資料庫查詢，userId={}", userId, e);
        }

        LambdaQueryWrapper<UmsUserInbox> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsUserInbox::getToUserId, userId)
                    .eq(UmsUserInbox::getDeleted, 0); // 邏輯未刪除

        if (Boolean.TRUE.equals(onlyUnread)) {
            queryWrapper.eq(UmsUserInbox::getReadFlag, 0);
        }

        // (最新的在最前)
        queryWrapper.orderByDesc(UmsUserInbox::getCreateAt);

        Page<UmsUserInbox> result = this.page(page, queryWrapper);
        List<UmsUserInbox> records = result.getRecords();
        if (records != null && !records.isEmpty()) {
            for (UmsUserInbox inbox : records) {
                if (inbox == null) {
                    continue;
                }
                inbox.setFromAvatar(AvatarUrlHelper.toPublicUrl(inbox.getFromAvatar(), minioDomain));
            }
        }
        return result;
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        LambdaQueryWrapper<UmsUserInbox> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UmsUserInbox::getToUserId, userId)
                    .eq(UmsUserInbox::getDeleted, 0)
                    .eq(UmsUserInbox::getReadFlag, 0);
        
        return (int) this.count(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long userId, Long inboxId) {
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<UmsUserInbox> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UmsUserInbox::getId, inboxId)
                     .eq(UmsUserInbox::getToUserId, userId)
                     .eq(UmsUserInbox::getDeleted, 0)
                     .set(UmsUserInbox::getReadFlag, 1) // 設為已讀
                     .set(UmsUserInbox::getReadAt, now)
                     .set(UmsUserInbox::getDeleted, 1) // 同步進行邏輯刪除
                     .set(UmsUserInbox::getUpdateAt, now);
        
        this.update(updateWrapper);

        // 同步更新 Redis 快取：將該通知從清單移除(失敗不影響主流程)
        try {
            String inboxKey = RedisCacheKey.USER_INBOX.format(userId);
            RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
            List<UmsUserInbox> cachedList = rList.readAll();
            if (cachedList != null && !cachedList.isEmpty()) {
                for (int i = 0; i < cachedList.size(); i++) {
                    UmsUserInbox inbox = cachedList.get(i);
                    if (inbox == null || inbox.getId() == null || inboxId == null) {
                        continue;
                    }
                    if (!inboxId.equals(inbox.getId())) {
                        continue;
                    }

                    rList.remove(i);
                    break;
                }
            }
        } catch (Exception e) {
            log.warn("同步移除收件匣快取訊息失敗(不影響主流程)，userId={}，inboxId={}", userId, inboxId, e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        LambdaUpdateWrapper<UmsUserInbox> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UmsUserInbox::getToUserId, userId)
                     .eq(UmsUserInbox::getDeleted, 0)  // 只更新未刪除的
                     .eq(UmsUserInbox::getReadFlag, 0) // 只更新原本未讀的
                     .set(UmsUserInbox::getReadFlag, 1)
                     .set(UmsUserInbox::getReadAt, now)
                     .set(UmsUserInbox::getDeleted, 1) // 同步進行邏輯刪除
                     .set(UmsUserInbox::getUpdateAt, now);
        
        this.update(updateWrapper);

        // 同步清空 Redis 快取(失敗不影響主流程)
        try {
            String inboxKey = RedisCacheKey.USER_INBOX.format(userId);
            RList<UmsUserInbox> rList = redissonClient.getList(inboxKey);
            rList.delete();
        } catch (Exception e) {
            log.warn("同步清空收件匣快取失敗(不影響主流程)，userId={}", userId, e);
        }
    }
}
