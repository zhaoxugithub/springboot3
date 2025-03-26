package com.atguigu.ioc2;

@PaymentService(PaymentType.ALIPAY)
public class AlipayStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("使用支付宝支付: %.2f 元%n", amount);
    }
}