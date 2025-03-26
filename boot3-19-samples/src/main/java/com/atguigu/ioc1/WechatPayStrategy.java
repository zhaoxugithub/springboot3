package com.atguigu.ioc1;

import org.springframework.stereotype.Component;

@Component("wechatPay")
public class WechatPayStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("使用微信支付: %.2f 元%n", amount);
    }
}