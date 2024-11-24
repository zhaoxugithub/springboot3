package com.serendipity.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("execution(* com.serendipity.controller..*(..))")
    public Object calculateExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();
        stopWatch.prettyPrint();
        System.out.println("Execution time of " + joinPoint.getSignature()
                                                           .getName() + " is " + stopWatch.getTotalTimeMillis() + " " +
                "ms");
        return result;
    }
}
