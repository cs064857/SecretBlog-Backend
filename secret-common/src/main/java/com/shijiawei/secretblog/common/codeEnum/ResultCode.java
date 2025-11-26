package com.shijiawei.secretblog.common.codeEnum;

import com.shijiawei.secretblog.common.exception.BusinessRuntimeException;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpStatus;

import java.util.Map;

/**
 * ClassName: ResultCode
 * Description:
 *
 * @Create 2025/11/20 下午11:30
 */
@Getter
public enum ResultCode implements IErrorCode{

    /**
     * 1000~1999 通用模組
     */
    SUCCESS(HttpStatus.OK,"1000", "操作成功"),
    PARAM_ERROR(HttpStatus.BAD_REQUEST,"1001", "參數格式錯誤"),
    PARAM_MISSING(HttpStatus.BAD_REQUEST,"1002", "缺少必要參數"),
    //執行緒被中斷
    THREAD_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR,"1003", "系統服務繁忙，請稍後再試"),
    SYSTEM_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"1004", "系統服務繁忙，請稍後再試"),
    REPEAT_OPERATION(HttpStatus.BAD_REQUEST, "1005", "請勿重複操作"),
    //執行修改操作失敗
    UPDATE_FAILED(HttpStatus.BAD_REQUEST,"1006", "系統服務繁忙，請稍後再試"),
    //執行新增操作失敗
    CREATE_FAILED(HttpStatus.BAD_REQUEST,"1007", "系統服務繁忙，請稍後再試"),
    DELETE_FAILED(HttpStatus.BAD_REQUEST,"1008", "系統服務繁忙，請稍後再試"),
    NOT_FOUND(HttpStatus.NOT_FOUND,"1008", "系統服務繁忙，請稍後再試"),
    /**
     * 2000~2999 文章模組
     */
    ARTICLE_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "2004", "文章服務內部異常"),

    /**
     * 3000~3999 網關模組
     */


    /**
     * 4000~4999 用戶模組
     */
    //用戶認證服務異常
    JWT_CONFIG_ERROR(HttpStatus.FORBIDDEN,"4002", "系統服務繁忙，請稍後再試"),
    //尚未登入或登入狀態已失效
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"4003", "請重新登入"),
    //用戶權限不足
    FORBIDDEN(HttpStatus.FORBIDDEN,"4004", "您沒有權限執行此操作"),
    // 用戶認證流程中的內部錯誤 (例如：Principal 轉換失敗、上下文丟失)
    AUTH_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "4005", "認證服務內部異常"),
    JWT_BLACKLISTED(HttpStatus.UNAUTHORIZED, "4006", "憑證已失效，請重新登入"),
    USER_INTERNAL_ERROR(HttpStatus.NOT_FOUND,"4007", "系統服務繁忙，請稍後再試"),
    USER_EMAIL_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "4009", "此電子郵件地址已被註冊"),

    /**
     * 5000~5999 系統/中間件錯誤
     */

    REDIS_INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"5001", "系統服務繁忙，請稍後再試"),
    //Redis鍵不存在
    REDIS_KEY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"5002", "系統服務繁忙，請稍後再試"),
    //Redis鍵格式化失敗，參數與模板不匹配
    REDIS_KEY_FORMAT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,"5003", "系統服務繁忙，請稍後再試"),
    //Redis鍵缺少必要參數
    REDIS_KEY_FORMAT_PARAM_MISSING(HttpStatus.INTERNAL_SERVER_ERROR,"5004", "系統服務繁忙，請稍後再試"),

    //布隆過濾器不存在
    BLOOM_FILTER_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"6001", "系統服務繁忙，請稍後再試"),


    //布隆過濾器鍵不存在
    BLOOM_FILTER_KEY_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,"6003", "系統服務繁忙，請稍後再試");
    //    BLOOM_FILTER_MISSING("2002", "缺少必要參數"),
//    BLOOM_FILTER_KEY_NOT_FOUND("2005", "布隆過濾器鍵不存在");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    ResultCode(HttpStatus httpStatus,String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
