package com.atguigu.boot.factory;

import org.springframework.stereotype.Component;

@Component
public class AliPayFactory implements PayFactory{
    @Override
    public void createPay() {
        System.out.println("创建支付宝支付...");
    }
}
