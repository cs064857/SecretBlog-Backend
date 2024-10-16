package com.shijiawei.secretblog.article.AOP;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * ClassName: LogAspect
 * Description:
 *
 * @Create 2024/9/4 下午11:22
 */
@Aspect
@Component
@Slf4j
public class LogAspect {
    /**
     * 開啟紀錄方法執行時間
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("@annotation(com.shijiawei.secretblog.article.annotation.OpenLog)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        //執行原方法中的程式碼
        Object proceed = joinPoint.proceed();
        long end = (System.currentTimeMillis()-start);
        log.info("執行方法完畢:{},耗時:{}毫秒",joinPoint.getSignature().getName(),end);
        return proceed;
    }


}
