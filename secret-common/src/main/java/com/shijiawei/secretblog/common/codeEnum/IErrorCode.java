package com.shijiawei.secretblog.common.codeEnum;

import org.springframework.http.HttpStatus;

/**
 * ClassName: IErrorCode
 * Description:
 *
 * @Create 2025/11/24 上午1:30
 */
public interface IErrorCode {

    String getCode();
    String getMessage();
    HttpStatus getHttpStatus();

}
