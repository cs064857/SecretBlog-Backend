package com.shijiawei.secretblog.common.utils;

import com.alibaba.nacos.common.utils.JacksonUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ClassName: R
 * Description:
 *
 * @Create 2024/8/28 下午3:41
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
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
//        return (new R<String>(200, "ok", dataJson));
//    }

    /**
     * 使用泛型返回
     * @param data
     * @return
     * @param <T>
     */
    public static <T> R<T> ok(T data) {
        return(new R<T>(200, "ok", data));
    }
    public static  R ok() {
        return(new R(200, "ok"));
    }

    public static R error(String msg) {
        return (new R(500, "error", msg));
    }
    //    public static <T> R<T> fail(String msg) {
//        return(new R<T>(500, msg, null));
//    }


    public R(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
