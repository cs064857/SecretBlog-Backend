package com.shijiawei.secretblog.common.exception;

import com.shijiawei.secretblog.common.codeEnum.IErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serial;

/**
 * ClassName: CustomRuntimeException
 * Description: 自定義業務異常類,區分內部詳細訊息和用戶友善訊息
 *
 * @Create 2025/3/5 上午1:55
 */

@Getter
public class BusinessRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6134276485972461837L;


    private final IErrorCode iErrorCode;
    private final String detailMessage; // 給開發者看的詳細訊息
    private final Object data; // 附加數據,用於傳遞額外的上下文信息


    /**
     * 完整建構子,可指定所有參數
     * @param detailMessage 內部詳細訊息
     * @param cause 異常原因
     */
    @Builder
    public BusinessRuntimeException(IErrorCode iErrorCode, String detailMessage,
                                    Throwable cause, Object data) {
        super(detailMessage, cause);
        this.iErrorCode = iErrorCode;
        this.detailMessage = detailMessage;
        this.data = data;
    }


    //只傳 ErrorCode
    public BusinessRuntimeException(IErrorCode iErrorCode) {
        this(iErrorCode, iErrorCode.getMessage(), null, null);
    }

    //帶詳細日誌
    public BusinessRuntimeException(IErrorCode iErrorCode, String detailMessage) {
        this(iErrorCode, detailMessage, null, null);
    }

    //帶詳細日誌 + 數據
    public BusinessRuntimeException(IErrorCode iErrorCode, String detailMessage, Object data) {
        this(iErrorCode, detailMessage, null, data);
    }



//    public BusinessRuntimeException(IErrorCode iErrorCode, String detailMessage, Throwable cause, Object data) {
//        super(detailMessage, cause);
//        this.iErrorCode = iErrorCode;
//        this.detailMessage = detailMessage;
//        this.data = data;
//    }

//
//    /**
//     * 建構子:指定所有參數(不含異常原因)
//     * @param code 錯誤碼
//     * @param httpStatus HTTP狀態碼
//     * @param userMessage 用戶友善訊息
//     * @param detailMessage 內部詳細訊息
//     */
//    public BusinessRuntimeException(String code, HttpStatus httpStatus, String userMessage, String detailMessage) {
//        this(code, httpStatus, userMessage, detailMessage, null,null);
//    }
//
//
//    /**
//     * 建構子:userMessage 和 detailMessage 相同,使用預設 HttpStatus.BAD_REQUEST
//     * @param code 錯誤碼
//     * @param message 訊息(同時用於用戶和開發者)
//     */
//    public BusinessRuntimeException(String code, String message) {
//
//        this(code, HttpStatus.BAD_REQUEST, message, message, null,null);
//    }
//    /**
//     * 建構子:userMessage 和 detailMessage 相同, 錯誤碼默認為400 ,使用預設 HttpStatus.BAD_REQUEST
//     * @param message 訊息(同時用於用戶和開發者)
//     */
//    public BusinessRuntimeException(String message) {
//
//        this("400", HttpStatus.BAD_REQUEST, message, message, null,null);
//    }
//
//    /**
//     * 建構子:指定 code 和 HttpStatus
//     * @param code 錯誤碼
//     * @param httpStatus HTTP狀態碼
//     */
//    public BusinessRuntimeException(String code, HttpStatus httpStatus) {
//        this(code, httpStatus, code, code, null,null);
//    }
//
//    /**
//     * 建構子:指定 code、httpStatus、message,含異常原因
//     * @param code 錯誤碼
//     * @param httpStatus HTTP狀態碼
//     * @param cause 異常原因
//     * @param message 訊息(同時用於用戶和開發者)
//     */
//    public BusinessRuntimeException(String code, HttpStatus httpStatus, Throwable cause, String message) {
//        this(code, httpStatus, message, message, cause,null);
//    }
//
//    /**
//     * 建構子:指定 code、userMessage、HttpStatus,detailMessage 同 userMessage
//     * @param code 錯誤碼
//     * @param httpStatus HTTP狀態碼
//     * @param message 訊息(同時用於用戶和開發者)
//     */
//    public BusinessRuntimeException(String code, HttpStatus httpStatus, String message) {
//        this(code, httpStatus, message, message, null,null);
//    }
//
//    /**
//     * 建構子:使用預設 HttpStatus.BAD_REQUEST,含異常原因,默認訊息
//     * @param code 錯誤碼
//     * @param cause 異常原因
//     */
//    public BusinessRuntimeException(String code, Throwable cause) {
//        this(code, HttpStatus.BAD_REQUEST, cause, "系統異常，請稍後再試");
//    }
//    /**
//     * 建構子:使用預設 HttpStatus.BAD_REQUEST,含異常原因,默認訊息
//     * @param code 錯誤碼
//     * @param detailMessage 訊息
//     * @param cause 異常原因
//     */
//    public BusinessRuntimeException(String code, String detailMessage, Throwable cause) {
//        this(code, HttpStatus.BAD_REQUEST, "系統異常，請稍後再試", detailMessage, cause,null);
//    }
//
//    /**
//     * 建構子:使用預設 HttpStatus.BAD_REQUEST,指定不同的用戶訊息和詳細訊息
//     * @param code 錯誤碼
//     * @param userMessage 用戶友善訊息
//     * @param detailMessage 內部詳細訊息
//     */
//    public BusinessRuntimeException(String code, String userMessage, String detailMessage) {
//        this(code, HttpStatus.BAD_REQUEST, userMessage, detailMessage, null,null);
//    }
//
//    /**
//     * 建構子:使用預設 HttpStatus.BAD_REQUEST,指定不同的用戶訊息、詳細訊息和異常原因
//     * @param code 錯誤碼
//     * @param userMessage 用戶友善訊息
//     * @param detailMessage 內部詳細訊息
//     * @param cause 異常原因
//     */
//    public BusinessRuntimeException(String code, String userMessage, String detailMessage, Throwable cause) {
//        this(code, HttpStatus.BAD_REQUEST, userMessage, detailMessage, cause,null);
//    }
//
//    /**
//     * 建構子:使用預設 HttpStatus.BAD_REQUEST,含異常原因
//     * @param code 錯誤碼
//     * @param cause 異常原因
//     * @param userMessage 訊息(同時用於用戶和開發者)
//     * @param detailMessage 訊息(同時用於用戶和開發者)
//     */
//    public BusinessRuntimeException(String code, Throwable cause, String userMessage, String detailMessage) {
//
//        this(code, HttpStatus.BAD_REQUEST, userMessage, detailMessage, cause,null);
//    }
}
