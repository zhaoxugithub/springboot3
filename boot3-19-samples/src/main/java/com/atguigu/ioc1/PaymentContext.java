package com.atguigu.ioc1;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PaymentContext {
    // 查找容器中所有 PaymentStrategy 类型的 Bean
    // 以 Bean 名称 作为 Key，Bean 实例作为 Value
    // 自动组装成 Map<String, PaymentStrategy>
    // 注入到构造器参数 strategyMap 中
    private final Map<String, PaymentStrategy> strategies;

    // 通过构造器注入所有PaymentStrategy实现
    public PaymentContext(Map<String, PaymentStrategy> strategyMap) {
        this.strategies = strategyMap;
    }

    public void executePayment(String paymentType, double amount) {
        PaymentStrategy strategy = strategies.get(paymentType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentType);
        }
        strategy.pay(amount);
    }
}
