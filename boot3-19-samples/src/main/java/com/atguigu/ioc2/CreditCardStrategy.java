package com.atguigu.ioc2;

@PaymentService(PaymentType.CREDIT_CARD)
public class CreditCardStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("使用信用卡支付: %.2f 元%n", amount);
    }
}
