package com.shijiawei.secretblog.common.exception;

import com.shijiawei.secretblog.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Timestamp;
import java.time.Instant;

/**
 * ClassName: MyControllerAdvice
 * Description:
 *
 * @Create 2025/8/19 上午2:57
 */
@Slf4j
@ControllerAdvice
public class MyControllerAdvice {

    @ResponseBody
    @ExceptionHandler(value = BusinessRuntimeException.class)
    public ResponseEntity<R<Void>> businessRuntimeExceptionHandler(BusinessRuntimeException e){
        Timestamp timestamp = Timestamp.from(Instant.now());
        // 預設抓第0個，防止極端情況下堆疊為空
        StackTraceElement stackTraceElement = e.getStackTrace()[0];

        // 遍歷堆疊軌跡，尋找真正的業務類位置，排除BusinessRuntimeException本身
        for (StackTraceElement element : e.getStackTrace()) {
            if(!element.getClassName().contains(BusinessRuntimeException.class.getSimpleName())){
                stackTraceElement = element;
                break;
            }
        }


        String location = String.format("%s.%s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        log.warn("warn: location:{}, code={}, msg={} , detailsMsg={} , timeStamp:{}, data:{}"
                ,location, e.getIErrorCode().getCode(), e.getIErrorCode().getMessage(), e.getDetailMessage(), timestamp,e.getData());

        R<Void> r = R.error(e.getIErrorCode().getCode(),e.getIErrorCode().getMessage(), timestamp);
        return new ResponseEntity<>(r,e.getIErrorCode().getHttpStatus());

    }
    @ResponseBody
    @ExceptionHandler(value = BusinessException.class)
    public ResponseEntity<R<Void>> businessExceptionHandler(BusinessException e){
        Timestamp timestamp = Timestamp.from(Instant.now());
        // 預設抓第0個，防止極端情況下堆疊為空
        StackTraceElement stackTraceElement = e.getStackTrace()[0];

        // 遍歷堆疊軌跡，尋找真正的業務類位置，排除BusinessException本身
        for (StackTraceElement element : e.getStackTrace()) {
            if(!element.getClassName().contains(BusinessException.class.getSimpleName())){
                stackTraceElement = element;
                break;
            }
        }


        String location = String.format("%s.%s:%d", stackTraceElement.getClassName(), stackTraceElement.getMethodName(), stackTraceElement.getLineNumber());
        log.error("error: location:{}, code={}, msg={} , detailsMsg={} , timeStamp:{}, data:{}"
                ,location, e.getIErrorCode().getCode(), e.getIErrorCode().getMessage(), e.getDetailMessage(), timestamp,e.getData());

        R<Void> r = R.error(e.getIErrorCode().getCode(),e.getIErrorCode().getMessage(), timestamp);
        return new ResponseEntity<>(r,e.getIErrorCode().getHttpStatus());

    }

}
