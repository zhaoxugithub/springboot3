package com.atguigu.ioc2;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

// 自定义注解标记策略类型
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface PaymentService {
    PaymentType value();
}