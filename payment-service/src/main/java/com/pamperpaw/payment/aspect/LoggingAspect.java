package com.pamperpaw.payment.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Around("execution(* com.pamperpaw.payment.service.impl.*.*(..))")
    public Object logServiceMethods(ProceedingJoinPoint joinPoint) throws Throwable {

        long startTime = System.currentTimeMillis();

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("Entering {}.{}", className, methodName);

        try {

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            log.info(
                    "Exiting {}.{} | Execution Time: {} ms",
                    className,
                    methodName,
                    executionTime
            );

            return result;

        } catch (Exception ex) {

            log.error(
                    "Exception in {}.{} : {}",
                    className,
                    methodName,
                    ex.getMessage()
            );

            throw ex;
        }
    }
}