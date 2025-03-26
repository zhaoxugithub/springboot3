package com.atguigu.ioc1;

import org.springframework.stereotype.Component;

@Component("creditCard")
public class CreditCardStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("使用信用卡支付: %.2f 元%n", amount);
    }
}
