package com.atguigu.aop1.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(3)
public class LoggingAspect {

    @AfterReturning(pointcut = "execution(* com.atguigu.aop1.service.OrderService.*(..))", returning = "result")
    public void logSuccess(Object result) {
        System.out.println("【日志打印】操作成功，订单ID: " + result);
    }

    @AfterThrowing(pointcut = "execution(* com.atguigu.aop1.service.OrderService.*(..))", throwing = "ex")
    public void logError(Exception ex) {
        System.out.println("【日志打印】操作失败，原因: " + ex.getMessage());
    }
}
