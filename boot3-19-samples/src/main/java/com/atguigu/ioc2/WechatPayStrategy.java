package com.atguigu.ioc2;

@PaymentService(PaymentType.WECHAT_PAY)
public class WechatPayStrategy implements PaymentStrategy {
    @Override
    public void pay(double amount) {
        System.out.printf("使用微信支付: %.2f 元%n", amount);
    }
}
