package com.atguigu.aop1.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class PermissionCheckAspect {

    @Around("execution(* com.atguigu.aop1.service.OrderService.*(..))")
    public Object checkPermission(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("【权限校验】开始验证权限...");
        if (!checkAuth()) {
            throw new SecurityException("权限不足，拒绝访问！");
        }
        Object proceed = joinPoint.proceed();// 继续执行后续切面或业务方法
        System.out.println("over.");
        return proceed;
    }

    private boolean checkAuth() {
        // 模拟权限验证逻辑，返回true表示有权限
        return true;
    }
}