package com.shijiawei.secretblog.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.R;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import com.shijiawei.secretblog.user.entity.UmsUserInbox;
import com.shijiawei.secretblog.user.service.UmsUserInboxService;
import com.shijiawei.secretblog.user.utils.SseClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 使用者通知收件匣 API
 */
@Slf4j
@RestController
@Tag(name = "使用者通知收件匣", description = "站內信通知相關的操作")
@RequestMapping("/ums/user/inbox")
public class UmsUserInboxController {

    @Autowired
    private UmsUserInboxService umsUserInboxService;

    @Autowired
    private SseClient sseClient;

    /**
     * 獲取收件匣列表(分頁)
     *
     * @param routePage 頁碼(預設1)
     * @param onlyUnread 是否只看未讀(預設false)
     * @return 分頁結果
     */
    @Operation(summary = "獲取收件匣列表", description = "支援分頁與未讀篩選")
    @GetMapping("/list")
    public R<Page<UmsUserInbox>> getInboxList(
            @RequestParam(defaultValue = "1") Integer routePage,
            @RequestParam(required = false) Boolean onlyUnread) {
        
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .build();
        }

        Page<UmsUserInbox> page = umsUserInboxService.getUserInboxPage(userId, routePage, onlyUnread);
        return R.ok(page);
    }

    /**
     * 獲取未讀訊息數量(用於紅點顯示)
     *
     * @return 未讀數量
     */
    @Operation(summary = "獲取未讀數量", description = "用於前端顯示通知紅點")
    @GetMapping("/unread-count")
    public R<Integer> getUnreadCount() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
             throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .build();
        }

        Integer count = umsUserInboxService.getUnreadCount(userId);
        return R.ok(count);
    }

    /**
     * 標記單則訊息為已讀並從收件匣移除(邏輯刪除)
     *
     * @param id 訊息ID
     * @return 結果
     */
    @Operation(summary = "標記單則已讀並移除", description = "將通知設為已讀後，會同步做邏輯刪除(deleted=1)，並從 Redis 收件匣快取清單移除")
    @PutMapping("/read/{id}")
    public R<Void> markAsRead(@PathVariable Long id) {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
             throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .build();
        }

        umsUserInboxService.markAsRead(userId, id);
        return R.ok();
    }

    /**
     * 標記所有訊息為已讀(一鍵已讀)
     *
     * @return 結果
     */
    @Operation(summary = "標記所有已讀")
    @PutMapping("/read-all")
    public R<Void> markAllAsRead() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
             throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .build();
        }

        umsUserInboxService.markAllAsRead(userId);
        return R.ok();
    }

//    /**
//     * 測試 SSE 推送功能
//     * 模擬一條「文章被回覆」通知，推送給當前登入使用者
//     *
//     * @return 推送結果
//     */
//    @Operation(summary = "測試 SSE 推送", description = "用於開發測試，模擬推送一條通知至前端")
//    @PreAuthorize("isAuthenticated()")
//    @PostMapping("/test-sse")
//    public R<Map<String, Object>> testSsePush() {
//        Long userId = UserContextHolder.getCurrentUserId();
//        if (userId == null) {
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.UNAUTHORIZED)
//                    .build();
//        }
//
//        // 構建模擬的通知 payload
//        UmsUserInbox mockInbox = UmsUserInbox.builder()
//                .id(System.currentTimeMillis()) // 使用時間戳作為模擬 ID
//                .toUserId(userId)
//                .fromUserId(0L) // 系統通知
//                .fromAvatar("user-assets-pub/default.svg")
//                .fromNickName("系統通知")
//                .type("ARTICLE_REPLIED")
//                .subject("測試文章標題")
//                .body("這是一條 SSE 測試推送訊息")
//                .articleId(0L)
//                .readFlag(0)
//                .deleted(0)
//                .createAt(LocalDateTime.now())
//                .updateAt(LocalDateTime.now())
//                .build();
//
//        boolean pushed = sseClient.sendMessage(
//                "ARTICLE_REPLIED",
//                String.valueOf(userId),
//                String.valueOf(mockInbox.getId()),
//                mockInbox
//        );
//
//        log.info("SSE 測試推送結果，userId={}，pushed={}", userId, pushed);
//
//        return R.ok(Map.of(
//                "pushed", pushed,
//                "message", pushed ? "SSE 推送成功" : "SSE 推送失敗(使用者可能未建立 SSE 連線)"
//        ));
//    }
}
