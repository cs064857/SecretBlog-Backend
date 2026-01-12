package com.shijiawei.secretblog.user.controller;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import com.shijiawei.secretblog.user.utils.SseClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

/**
 * 使用者SSE相關端點。
 *
 * @Create 2026/1/9
 */
@Slf4j
@RestController
@RequestMapping("/ums/user/sse")
public class UmsSseController {

    @Autowired
    private SseClient sseClient;

    /**
     * 訂閱 SSE，連線會綁定到目前登入者的 userId
     */
    @GetMapping("/subscribe")
    public SseEmitter subscribe() {
        return sseClient.createSse();
    }



//    /**
//     * 測試SSE推送端點，前端訂閱SSE後，呼叫此端點可測試訊息推送功能。
//     * @param message 要推送的訊息內容
//     * @return 推送結果
//     */
//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/push-test")
//    public ResponseEntity<String> pushTestMessage(
//            @RequestParam(required = false, defaultValue = "這是一則SSE測試訊息") String message) {
//
//        Long userId = UserContextHolder.getCurrentUserId();
//        if (userId == null) {
//            throw BusinessRuntimeException.builder()
//                    .iErrorCode(ResultCode.UNAUTHORIZED)
//                    .detailMessage("用戶未登入，無法推送測試訊息")
//                    .build();
//        }
//
//        boolean sent = sseClient.sendMessage(
//                "test-event",
//                String.valueOf(userId),
//                UUID.randomUUID().toString(),
//                message
//        );
//
//        if (sent) {
//            log.info("SSE 測試推送成功，userId={}，message={}", userId, message);
//            return ResponseEntity.ok("推送成功: " + message);
//        } else {
//            log.warn("SSE 測試推送失敗，userId={}(可能尚未建立SSE連線)", userId);
//            return ResponseEntity.status(500).body("推送失敗：尚未建立SSE連線，請先呼叫 /subscribe");
//        }
//    }

    /**
     * 關閉目前登入者的SSE連線。
     */
    @GetMapping("/closeSse")
    public void closeConnect() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，無法關閉SSE連線")
                    .build();
        }
        sseClient.closeSse(String.valueOf(userId));
    }
}
