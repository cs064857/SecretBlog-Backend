package com.shijiawei.secretblog.common.exception;

import com.shijiawei.secretblog.common.codeEnum.IErrorCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;

import java.io.Serial;

/**
 * ClassName: CustomRuntimeException
 * Description: 自定義異常類,區分內部詳細訊息和用戶友善訊息
 *
 * @Create 2025/3/5 上午1:55
 */

@Getter
public class BusinessException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 6119756125972432487L;


    private final IErrorCode iErrorCode;
    private final String detailMessage; // 給開發者看的詳細訊息
    private final Object data; // 附加數據,用於傳遞額外的上下文信息


    /**
     * 完整建構子,可指定所有參數
     * @param detailMessage 內部詳細訊息
     * @param cause 異常原因
     */
    @Builder
    public BusinessException(IErrorCode iErrorCode, String detailMessage,
                                    Throwable cause, Object data) {
        super(detailMessage, cause);
        this.iErrorCode = iErrorCode;
        this.detailMessage = detailMessage;
        this.data = data;
    }


    //只傳 ErrorCode
    public BusinessException(IErrorCode iErrorCode) {
        this(iErrorCode, iErrorCode.getMessage(), null, null);
    }

    //帶詳細日誌
    public BusinessException(IErrorCode iErrorCode, String detailMessage) {
        this(iErrorCode, detailMessage, null, null);
    }

    //帶詳細日誌 + 數據
    public BusinessException(IErrorCode iErrorCode, String detailMessage, Object data) {
        this(iErrorCode, detailMessage, null, data);
    }

}
