package com.shijiawei.secretblog.common.exception;

import com.shijiawei.secretblog.common.exception.CustomBaseException;
import org.springframework.http.HttpStatus;

/**
 * 拋異常的工具類
 */
public class ExceptionTool {

    private static final HttpStatus defaultHttpStatus = HttpStatus.BAD_REQUEST;

    public static void throwException(String message) {
        throw new CustomBaseException(message, defaultHttpStatus);
    }

    public static void throwException(boolean throwException, String message) {
        if (throwException) {
            throw new CustomBaseException(message, defaultHttpStatus);
        }
    }

    public static void throwException(HttpStatus httpStatus) {
        throw new CustomBaseException(httpStatus);
    }

    public static void throwException(String message, String code) {
        CustomBaseException CustomBaseException = new CustomBaseException(message, defaultHttpStatus);
        CustomBaseException.setCode(code);
        throw CustomBaseException;
    }

    public static void throwException(String message, HttpStatus httpStatus) {
        throw new CustomBaseException(message, httpStatus);
    }

    public static void throwException(String message, Throwable e) {
        throw new CustomBaseException(message, e, defaultHttpStatus);
    }

    public static void throwException(String message, HttpStatus httpStatus, String errorCode) {
        CustomBaseException CustomBaseException = new CustomBaseException(message, httpStatus);
        CustomBaseException.setCode(errorCode);
        throw CustomBaseException;
    }
}
