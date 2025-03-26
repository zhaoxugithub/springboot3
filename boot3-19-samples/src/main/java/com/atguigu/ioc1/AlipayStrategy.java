package com.atguigu.ioc1;

import org.springframework.stereotype.Component;

@Component("alipay") // 指定Bean名称
public class AlipayStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("使用支付宝支付: %.2f 元%n", amount);
    }
}
