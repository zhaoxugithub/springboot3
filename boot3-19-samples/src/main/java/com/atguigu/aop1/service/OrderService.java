package com.atguigu.aop1.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    // 业务人员只需专注此核心逻辑
    public String createOrder() {
        System.out.println("【业务逻辑】创建订单中...");
        // 模拟业务操作，返回订单ID
        return "ORD-123456";
    }

    public void updateOrder(String orderId) {
        System.out.println("【业务逻辑】更新订单中...");
        throw new RuntimeException("更新订单失败");
    }

}
