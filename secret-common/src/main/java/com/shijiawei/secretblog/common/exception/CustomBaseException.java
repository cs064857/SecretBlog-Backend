package com.shijiawei.secretblog.common.exception;

import org.springframework.http.HttpStatus;

import java.util.IllegalFormatException;

/**
 * ClassName: CustomBaseException
 * Description:
 *
 * @Create 2025/3/5 上午1:55
 */


public class CustomBaseException extends RuntimeException{

    private static final long serialVersionUID = 6134276485972461837L;
    private String code;
    private String message;
    private HttpStatus httpStatus;
    private Throwable e;
    public CustomBaseException(String code, String message, HttpStatus httpStatus) {
//        super(message);
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public CustomBaseException(String code, Throwable e, HttpStatus httpStatus) {
        this.code = code;
        this.e=e;
        this.httpStatus = httpStatus;
    }

    public CustomBaseException(String code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }
    public CustomBaseException(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }


    public CustomBaseException(String code) {
        this.code = code;
    }

    public CustomBaseException(String message, Throwable e) {
        this.message=message;
        this.e=e;

    }
//    public CustomBaseException(String Message) {
//        super(Message);
//    }

    public HttpStatus getHttpStatus(){
        return this.httpStatus;
    }

    public String getCode(){
        return this.code;
    }

    public String getMessage(){
        return this.message;
    }

    public void setCode(String code) {
    }
}
