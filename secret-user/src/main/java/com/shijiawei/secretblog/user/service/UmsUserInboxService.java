package com.shijiawei.secretblog.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.shijiawei.secretblog.user.entity.UmsUserInbox;
import java.util.List;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 使用者通知收件匣(Inbox)服務介面
 */
public interface UmsUserInboxService extends IService<UmsUserInbox> {
    
    /**
     * 分頁查詢使用者的收件匣
     * @param userId 使用者ID
     * @param routePage 頁碼
     * @param onlyUnread 是否只看未讀
     * @return 分頁結果
     */
    Page<UmsUserInbox> getUserInboxPage(Long userId, Integer routePage, Boolean onlyUnread);

    /**
     * 獲取使用者的未讀訊息數量
     * @param userId 使用者ID
     * @return 未讀數量
     */
    Integer getUnreadCount(Long userId);

    /**
     * 標記單則訊息已讀
     * @param userId 使用者ID(驗證用)
     * @param inboxId 訊息ID
     */
    void markAsRead(Long userId, Long inboxId);

    /**
     * 標記該使用者所有訊息為已讀
     * @param userId 使用者ID
     */
    void markAllAsRead(Long userId);
}

