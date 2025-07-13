package org.example.autopark.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(PerformanceLoggingAspect.class);

    @Value("${performance.logging.enabled:true}")
    private boolean enabled;

    @Around("@annotation(org.example.autopark.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!enabled) return joinPoint.proceed();

        long start = System.nanoTime();
        Object proceed = joinPoint.proceed();
        long duration = System.nanoTime() - start;

        logger.info("[PERF] {} executed in {} ms", joinPoint.getSignature(), duration / 1_000_000);
        return proceed;
    }
}