package com.shijiawei.secretblog.common.utils;

import com.alibaba.nacos.common.utils.JacksonUtils;
import com.shijiawei.secretblog.common.codeEnum.HttpCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.sql.Timestamp;
import java.util.HashMap;

/**
 * ClassName: R
 * Description:
 *
 * @Create 2024/8/28 下午3:41
 */
@Data

public class R<T> {
    private String code;
    private String msg;
    private T data;
    private Timestamp timestamp;
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
        return (new R<>(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS.getDescription(), data));
    }

    public static <T> R<T> ok(String code,T data) {
        return (new R<>(HttpCodeEnum.SUCCESS.getCode(), code, data));
    }

//    public static <T> R<T> ok(T data) {
//        R<T> r = new R<T>();
//        r.put("code", HttpCodeEnum.SUCCESS.getCode());
//        r.put("data", data);
//        return r;
//    }


    public static R<Void> ok() {
        return (new R<>(HttpCodeEnum.SUCCESS.getCode(), HttpCodeEnum.SUCCESS.getDescription()));
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
    public static <T> R<T> error() {
        return (new R<>(HttpCodeEnum.OPERATION_ERR.getCode(), HttpCodeEnum.OPERATION_ERR.getDescription()));
    }
    public static <T> R<T> error(String code, String msg, Timestamp timestamp){
        return new R<>(code, msg,null,timestamp);
    }
    public static <T> R<T> error(String code, String msg, Timestamp timestamp,T data){
        return new R<>(code, msg,data,timestamp);
    }
    public static <T> R<T> error(String code , Timestamp timestamp) {
        return new R<T>(HttpCodeEnum.OPERATION_ERR.getCode(), code,null,timestamp);
    }

    public static <T> R<T> error(String code) {
        return new R<T>(HttpCodeEnum.OPERATION_ERR.getCode(), code);
    }

    /**
     * 錯誤,參數為自訂訊息、校驗異常資訊(校驗屬性與錯誤原因)
     *
     * @param errorData
     * @param <T>
     * @return
     */
    public static <T> R<T> error(String msg, T errorData, Timestamp timestamp) {
        return new R<T>(HttpCodeEnum.OPERATION_ERR.getCode(), msg, errorData);
    }

    public R() {
    }

    public R(String msg) {
        this.msg = msg;
    }

    public R(String code, T data) {
        this.code = code;
        this.data = data;
    }

    public R(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public R(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
    public R(String code, String msg, T data, Timestamp timestamp) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.timestamp = timestamp;
    }

}
