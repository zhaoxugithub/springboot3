package com.atguigu.ioc2;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PaymentContext {
    private final Map<PaymentType, PaymentStrategy> strategyMap;

    public PaymentContext(List<PaymentStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .filter(s -> s.getClass().isAnnotationPresent(PaymentService.class))
                .collect(Collectors.toMap(s -> s.getClass().getAnnotation(PaymentService.class).value(),
                        Function.identity()));
    }

    public void executePayment(PaymentType paymentType, double amount) {
        PaymentStrategy strategy = strategyMap.get(paymentType);
        if (strategy == null) {
            throw new IllegalArgumentException("不支持的支付方式: " + paymentType);
        }
        strategy.pay(amount);
    }
}
