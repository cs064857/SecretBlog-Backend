//package com.shijiawei.secretblog.user.AOP;
//
//import com.alibaba.fastjson2.JSON;
//import com.shijiawei.secretblog.common.annotation.OverwriteCache;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.redisson.api.RedissonClient;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.DefaultParameterNameDiscoverer;
//import org.springframework.expression.ParserContext;
//import org.springframework.expression.spel.standard.SpelExpressionParser;
//import org.springframework.expression.spel.support.StandardEvaluationContext;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.Method;
//import java.time.Duration;
//import java.time.temporal.ChronoUnit;
//
///**
// * ClassName: OverwriteCacheAspect
// * Description:
// *
// * @Create 2025/2/18 下午11:48
// */
//@Aspect
//@Component
//@Slf4j
//public class OverwriteCacheAspect {
//
//    @Autowired
//    private RedissonClient redissonClient;
//    @Autowired
//    private SpelExpressionParser parser;
//    @Autowired
//    private StandardEvaluationContext standardEvaluationContext;
//
//    private final DefaultParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();
//
//    @Around("@annotation(com.shijiawei.secretblog.common.annotation.OverwriteCache)")
//    public Object overwriteCacheAspect(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
//        log.info("overwriteCacheAspect");
//        MethodSignature methodSignature = (MethodSignature)proceedingJoinPoint.getSignature();
//        Method method = methodSignature.getMethod();
//        OverwriteCache overwriteCache = method.getAnnotation(OverwriteCache.class);
//
//        Object[] args = proceedingJoinPoint.getArgs();
//
//        //保存的桶名
//        String prefix = overwriteCache.prefix();
//        String keyExpression = overwriteCache.key();
//        //保存時間
//        int time = overwriteCache.time();
//        ChronoUnit chronoUnit = overwriteCache.chronoUnit();
//        Duration duration = Duration.of(time,chronoUnit);
//
//
//        // 若包含 SpEL 表達式
//        // joinPoint.getArgs()得到具體的參數值，例如[0]為3415518(categoryId的值)、[1]為1(routePage的值)
//        if (keyExpression.contains("#")) {
//            keyExpression = parserSpEL(keyExpression,method,args);
//        }
//        String bucket = prefix+":"+keyExpression;
//
//        //從原方法中取得資料
//        String proceed = (String)proceedingJoinPoint.proceed();
//        if(!proceed.isEmpty()){
//            //轉為JSON保存至Redis中，會取代之前的數據，有設置過期時間
//            redissonClient.getBucket(bucket)
//                    .getAndSetAsync(JSON.toJSONString(proceed), duration);
//        }
//
//
//        return null;
//    }
//    public String parserSpEL(String keyExpression,Method method,Object[] args){
//
//
//        String[] parameterNames = nameDiscoverer.getParameterNames(method);
//        if (parameterNames == null) {
//            log.error("無法獲取方法參數名稱");
//            return keyExpression;
//        }
//        for (int i = 0; i < parameterNames.length; i++) {
//            standardEvaluationContext.setVariable(parameterNames[i],args[i]);
//        }
//
//        return parser.parseExpression(keyExpression, ParserContext.TEMPLATE_EXPRESSION).getValue(standardEvaluationContext, String.class);
//
//    }
//}
