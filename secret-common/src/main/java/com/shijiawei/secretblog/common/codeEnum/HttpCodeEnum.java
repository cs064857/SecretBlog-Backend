package com.shijiawei.secretblog.common.codeEnum;

import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * ClassName: HttpCodeEnum
 * Description:此枚舉類用於定義常用的結果回傳碼
 *
 * @Create 2024/9/4 上午1:44
 */
@Getter
@AllArgsConstructor
public enum HttpCodeEnum {

    /**
     * 操作成功
     */
    SUCCESS("200", "操作成功!"),

    //==================== 登入相關枚舉 ======================
    /**
     * 登入超時
     */
    LOGIN_TIMEOUT("100", "登入超時!"),

    /**
     * 用戶未登入
     */
    NOT_LOGGED_IN("101", "用戶未登入,請先進行登入!"),

    /**
     * "帳戶被禁用,請聯繫管理員解決
     */
    ACCOUNT_DISABLED("102", "帳戶被禁用,請聯繫管理員解決!"),

    /**
     * 用戶資訊加載失敗
     */
    LOAD_USER_ERR("103", "用戶資訊加載失敗!"),

    /**
     * 用戶身份資訊獲取失敗
     */
    GET_IDENTITY_ERR("104", "用戶身份資訊獲取失敗!"),

    /**
     * 用戶名不能為空
     */
    USERNAME_EMPTY("105", "用戶名不能為空!"),

    /**
     * 密碼不能為空
     */
    PASSWORD_EMPTY("106", "密碼不能為空!"),

    /**
     * 用戶名或密碼錯誤
     */
    LOGIN_FAIL("107", "用戶名或密碼錯誤!"),

    /**
     * 用戶登入成功
     */
    LOGIN_SUCCESS("108", "用戶登入成功!"),

    /**
     * 用戶註銷成功
     */
    LOGOUT_SUCCESS("109", "用戶註銷成功!"),

    //==================== 註冊相關枚舉 ======================

    /**
     * 驗證碼錯誤
     */
    CAPTCHA_ERR("300", "驗證碼錯誤!"),

    /**
     * 驗證碼過期
     */
    CAPTCHA_EXPIRED("301", "驗證碼已過期!"),

    /**
     * 用戶名已存在
     */
    USERNAME_EXISTS("302", "用戶名已存在!"),

    EMAIL_EXISTS("303","信箱已註冊!"),

    //======================= 其他枚舉 ==============================

    /**
     * 操作失敗
     */
    OPERATION_ERR("400", "操作失敗!"),

    /**
     * 沒有權限
     */
    NO_PERMISSION("403", "您沒有操作權限!"),

    /**
     * 頁面不存在
     */
    PAGE_NOT_FOUND("404", "未找到您請求的資源!"),

    /**
     * 請求方式錯誤
     */
    METHOD_ERR("405", "請求方式錯誤,請檢查後重試!"),

    /**
     * 請求限流
     */
    TOO_MANY_REQUESTS("406","請求次數過多，請稍後再試。"),

    /**
     * 參數格式不合法
     */
    VERIFY_ERR("500", "參數格式不合法,請檢查後重試!"),

    /**
     * 未知異常
     */
    UNKNOWN_ERR("600","未知異常");



    /**
     * 回傳碼
     */
    private final String code;
    /**
     * 描述
     */
    private final String description;
}

