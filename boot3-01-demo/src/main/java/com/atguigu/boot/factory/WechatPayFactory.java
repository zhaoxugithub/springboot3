package com.atguigu.boot.factory;

import org.springframework.stereotype.Component;

@Component
public class WechatPayFactory implements PayFactory{
    @Override
    public void createPay() {
        System.out.println("创建微信支付...");
    }
}
