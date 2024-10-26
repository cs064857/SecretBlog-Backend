package com.shijiawei.secretblog.common.utils;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.shijiawei.secretblog.common.codeEnum.HttpCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

/**
 * ClassName: R
 * Description:
 *
 * @Create 2024/8/28 下午3:41
 */
@Data

public class R<T> {
    private int code;
    private String msg;
    private T data;

//    /**
//     * 使用JSON返回
//     * @param data
//     * @return
//     * @param <T>
//     */
//    public static <T> R<String> ok(T data) {
//        String dataJson = JacksonUtils.toJson(data);
//        return (new R<String>(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS_ERR.getDescription(), dataJson));
//    }

    /**
     * 使用泛型返回
     *
     * @param data
     * @param <T>
     * @return
     */
    public static <T> R<T> ok(T data) {
        return (new R<T>(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS.getDescription(), data));
    }

//    public static <T> R<T> ok(T data) {
//        R<T> r = new R<T>();
//        r.put("code", HttpCodeEnum.SUCCESS.getCode());
//        r.put("data", data);
//        return r;
//    }


    public static R ok() {
        return (new R(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS.getDescription()));
    }

//    public static  R ok() {
//        R r = new R();
//        r.put("code", HttpCodeEnum.SUCCESS.getCode());
//        r.put("msg", HttpCodeEnum.SUCCESS.getDescription());
//        return r;
//    }

    /**
     * 錯誤,無參
     *
     * @return
     */
    public static R error() {
        return (new R(HttpCodeEnum.OPERATION_ERR.getCode(), HttpCodeEnum.OPERATION_ERR.getDescription()));
    }


    /**
     * 錯誤,參數為自訂訊息、校驗異常資訊(校驗屬性與錯誤原因)
     *
     * @param errorData
     * @param <T>
     * @return
     */
    public static <T> R<T> error(String msg, T errorData) {
        return new R<T>(HttpCodeEnum.OPERATION_ERR.getCode(), msg, errorData);
    }

    public R() {
    }

    public R(String msg) {
        this.msg = msg;
    }

    public R(String msg, T data) {
        this.msg = msg;
        this.data = data;
    }

    public R(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }


}
