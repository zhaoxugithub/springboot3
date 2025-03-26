package com.atguigu.ioc1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application implements CommandLineRunner {

    @Autowired
    private PaymentContext paymentContext;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        // 模拟支付操作
        paymentContext.executePayment("alipay", 199.99);
        paymentContext.executePayment("wechatPay", 299.00);
        paymentContext.executePayment("creditCard", 599.50);

        try {
            paymentContext.executePayment("paypal", 100.00);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
