package com.pamperpaw.auth.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

    @Around("execution(* com.pamperpaw.auth.controller..*(..)) || execution(* com.pamperpaw.auth.service..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String signature = joinPoint.getSignature().toShortString();
        log.info("Entering {}", signature);
        try {
            Object result = joinPoint.proceed();
            log.info("Exiting {}", signature);
            return result;
        } catch (Throwable throwable) {
            log.error("Error in {}", signature, throwable);
            throw throwable;
        }
    }
}
