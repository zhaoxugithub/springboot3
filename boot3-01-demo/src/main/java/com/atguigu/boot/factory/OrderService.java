package com.atguigu.boot.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {
    // Spring 会去寻找实现了 PayFactory 并且加了 @Component 的类
    @Autowired
    @Qualifier("wechatPayFactory") // 指定要哪一个实现类
    private PayFactory payFactory;

    @Autowired
    private PayFactory wechatPayFactory;

    @Autowired
    private List<PayFactory> payFactories;

    // Spring 会把所有 PayFactory 的实现类都塞进这个 Map 里
    // Key 是 Bean 的名字（如 "wechatPayFactory"），Value 是具体的实现类实例
    @Autowired
    private Map<String, PayFactory> payFactoryMap;

    @Autowired
    private final Map<String, PayFactory> payFactoryMap2 = new ConcurrentHashMap<String, PayFactory>();


    public void process() {
        payFactory.createPay();
    }

    public void process2() {
        wechatPayFactory.createPay();
    }

    public void process3() {
        for (PayFactory payFactory : payFactories) {
            payFactory.createPay();
        }
    }

    public void process4() {
        for (Map.Entry<String, PayFactory> entry : payFactoryMap.entrySet()) {
            System.out.println("Bean Name: " + entry.getKey());
            entry.getValue().createPay();
        }

        for (Map.Entry<String, PayFactory> entry : payFactoryMap2.entrySet()) {
            System.out.println("new Bean Name: " + entry.getKey());
            entry.getValue().createPay();
        }
    }
}
