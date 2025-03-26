package com.atguigu.aop1.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(2)
public class TransactionAspect {

    @Around("execution(* com.atguigu.aop1.service.OrderService.*(..))")
    public Object manageTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("【事务管理】开启事务...");
        try {
            Object result = joinPoint.proceed(); // 执行业务方法
            System.out.println("【事务管理】提交事务...");
            return result;
        } catch (Exception e) {
            System.out.println("【事务管理】回滚事务...");
            throw e;
        }
    }
}
