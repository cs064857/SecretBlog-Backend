package com.shijiawei.secretblog.common.exception;

import com.shijiawei.secretblog.common.utils.R;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * ClassName: MyControllerAdvice
 * Description:
 *
 * @Create 2025/8/19 上午2:57
 */
@ControllerAdvice
public class MyControllerAdvice {

    @ResponseBody
    @ExceptionHandler(value = CustomBaseException.class)
    public R myExceptionHandler(CustomBaseException e){

        return R.error(e.getCode(),e.getMessage());
    }

}
