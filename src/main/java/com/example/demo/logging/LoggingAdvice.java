package com.example.demo.logging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.demo.util.MetricRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class LoggingAdvice {

    @Autowired
    MetricRegistry metricRegistry;

    Logger logger = LoggerFactory.getLogger(LoggingAdvice.class);
    ObjectMapper mapper = new ObjectMapper();

    @Pointcut("execution(public * com.example.demo.controller.*.*(..)))")
    public void myPointCut() {

    }

    @Around("myPointCut()")
    public Object appLogger(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().getName();
        String className = pjp.getTarget().getClass().getName();
        //Doesnt work for multipart files
        try {
            logger.debug(className + ":" + method + "():Arguments:" + mapper.writeValueAsString(pjp.getArgs()));
        } catch (Exception e) {
        }
        Object obj = pjp.proceed();
        try {
            logger.debug(className + ":" + method + "():Response:" + mapper.writeValueAsString(obj));
        } catch (Exception e) {
        }
        return obj;
    }

    @AfterThrowing(value = "execution(public * com.example.demo.controller.*.*(..)))", throwing = "e")
    public void after(JoinPoint jp, Exception e) {
        try {
            logger.warn(jp.getSignature() + " throwed : " + e.getMessage());
            //+ "Args : " +mapper.writeValueAsString(jp.getArgs()));
        } catch (Exception ex) {
        }
    }

    @Around("@annotation(com.example.demo.logging.TrackExecutionTime) || " +
            "execution(public * com.example.demo.controller.*.*(..)))")
    public Object trackTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object obj = pjp.proceed();
        long end = System.currentTimeMillis();
        logger.info(pjp.getSignature() + " took : " + (end - start) + "ms");
        metricRegistry.getInstance().timer("userFiles.endpoint.http.time." + pjp.getSignature().getName()).record(end - start, TimeUnit.MILLISECONDS);
        return obj;
    }

    @Before(value = "execution(public * com.example.demo.controller.*.*(..)))")
    public void before(JoinPoint joinPoint) {
        metricRegistry.getInstance().counter("userFiles.endpoint.http." + joinPoint.getSignature().getName()).increment();
    }
}
