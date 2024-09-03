package com.shijiawei.secretblog.article.Exception;

import com.shijiawei.secretblog.common.codeEnum.HttpCodeEnum;
import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.MethodNotSupportedException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: GlobalExceptionHandler
 * Description:
 *
 * @Create 2024/9/3 下午6:22
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public R handleValidException(MethodArgumentNotValidException e) {
        Map<String, String> map = new HashMap<String, String>();
        List<ObjectError> allErrors = e.getAllErrors();
        allErrors.forEach(error -> {
            String fieldName=((FieldError)error).getField();
            String errorMsg=((FieldError)error).getDefaultMessage();
            map.put(fieldName, errorMsg);
        });

        log.error("數據校驗異常:{}",map);
        return R.error(HttpCodeEnum.VERIFY_ERR.getDescription(),map);
    }
}
