package com.shijiawei.secretblog.user.utils;

import com.shijiawei.secretblog.common.codeEnum.ResultCode;
import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import com.shijiawei.secretblog.common.utils.UserContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SSE 連線管理。
 *
 * @Create 2026/1/9
 */
@Slf4j
@Component
public class SseClient {

    private static final Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    /**
     * 建立 SSE 連線，並綁定到目前登入者的 userId。
     */
    public SseEmitter createSse() {
        Long userId = UserContextHolder.getCurrentUserId();
        if (userId == null) {
            throw BusinessRuntimeException.builder()
                    .iErrorCode(ResultCode.UNAUTHORIZED)
                    .detailMessage("用戶未登入，無法建立 SSE 連線")
                    .build();
        }

        String userKey = String.valueOf(userId);
        //設定為永不逾時
        SseEmitter sseEmitter = new SseEmitter(0L);

        //連線完成(正常關閉)後回呼
        sseEmitter.onCompletion(() -> {
            log.info("[{}] SSE 連線結束", userKey);
            sseEmitterMap.remove(userKey, sseEmitter);
        });

        //連線逾時回呼(目前 timeout 設為 0L，理論上不會觸發；保留以便未來調整)
        sseEmitter.onTimeout(() -> {
            log.info("[{}] SSE 連線逾時", userKey);
            sseEmitterMap.remove(userKey, sseEmitter);
            sseEmitter.complete();
        });

        //連線異常回呼
        sseEmitter.onError(throwable -> {
            log.warn("[{}] SSE 連線異常：{}", userKey, throwable.toString());
            sseEmitterMap.remove(userKey, sseEmitter);
        });

        //覆蓋舊連線(避免舊連線完成時誤移除新連線，移除時需比對 value)
        SseEmitter previous = sseEmitterMap.put(userKey, sseEmitter);
        if (previous != null && previous != sseEmitter) {
            try {
                previous.complete();
            } catch (Exception ignored) {
            }
        }

        //發送初始化事件(主要用於設定重連時間)
        try {
            sseEmitter.send(SseEmitter.event().reconnectTime(5000));
        } catch (IOException e) {
            log.warn("[{}] SSE 初始化事件傳送失敗：{}", userKey, e.getMessage());
        }

        log.info("[{}] 建立 SSE 連線成功", userKey);
        return sseEmitter;
    }



    /**
     * 對指定使用者推送 SSE 訊息。
     *
     * @param eventName 事件名稱(可為空，空則預設為 message)
     * @param userId 使用者識別
     * @param messageId 訊息 ID
     * @param message   訊息內容
     */
    public <T> boolean sendMessage(String eventName, String userId, String messageId, T message) {


        if (message instanceof String) {
            if (StringUtils.isBlank((String) message)) {
                log.info("[{}] 參數異常，message 為空", userId);
                return false;
            }
        } else {
            if (Objects.isNull(message)) {
                log.info("[{}] 參數異常，message 為空", userId);
                return false;
            }
        }

        SseEmitter sseEmitter = sseEmitterMap.get(userId);
        if (sseEmitter == null) {
            log.info("[{}] 訊息推送失敗，尚未建立 SSE 連線", userId);
            return false;
        }

        boolean hasEventName = StringUtils.isNotBlank(eventName);
        try {
            SseEmitter.SseEventBuilder sseEventBuilder = SseEmitter.event()
                    .id(messageId)
                    .reconnectTime(60_000L)
                    .data(message);

            // 有傳入 eventName 則進行設置，否則事件名稱預設為 message
            if (hasEventName) {
                sseEventBuilder.name(eventName);
            }

            sseEmitter.send(sseEventBuilder);
            log.info("事件名稱:{},使用者:{},消息id:{},推送成功:{}",
                    hasEventName ? eventName : "message", userId, messageId, message);
            return true;
        } catch (Exception e) {
            // 避免移除到已被覆蓋的新連線
            sseEmitterMap.remove(userId, sseEmitter);
            log.info("事件名稱:{},使用者:{},消息id:{},推送異常:{}",
                    hasEventName ? eventName : "message", userId, messageId, e.getMessage());
            sseEmitter.complete();
            return false;
        }
    }

    /**
     * 關閉指定使用者的 SSE 連線。
     * @param userId 使用者識別
     */
    public void closeSse(String userId) {
        if (StringUtils.isBlank(userId)) {
            return;
        }

        SseEmitter sseEmitter = sseEmitterMap.remove(userId);
        if (sseEmitter == null) {
            log.info("使用者 {} SSE 連線已關閉或不存在", userId);
            return;
        }

        sseEmitter.complete();
        log.info("使用者 {} SSE 連線已關閉", userId);
    }
}
