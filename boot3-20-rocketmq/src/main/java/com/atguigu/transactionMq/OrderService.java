package com.atguigu.transactionMq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 订单服务
 * 模拟本地业务数据库操作，用于演示事务消息的本地事务执行
 * 
 * @author atguigu
 */
@Slf4j
@Service
public class OrderService {
    
    /**
     * 使用ConcurrentHashMap模拟数据库存储订单数据
     * 实际生产环境中应该使用真实的数据库操作
     */
    private final Map<String, Order> orderDatabase = new ConcurrentHashMap<>();
    
    /**
     * 创建订单 - 模拟本地事务操作
     * 这个方法会在RocketMQ事务消息的本地事务中被调用
     * 
     * @param order 订单对象
     * @return 是否创建成功
     */
    public boolean createOrder(Order order) {
        try {
            // 模拟业务逻辑校验
            if (order.getAmount().doubleValue() <= 0) {
                log.error("订单金额不能为0或负数, orderId: {}", order.getOrderId());
                return false;
            }
            
            if (order.getQuantity() <= 0) {
                log.error("订单数量不能为0或负数, orderId: {}", order.getOrderId());
                return false;
            }
            
            // 模拟数据库插入操作
            orderDatabase.put(order.getOrderId(), order);
            log.info("订单创建成功, orderId: {}, userId: {}, productName: {}, amount: {}", 
                    order.getOrderId(), order.getUserId(), order.getProductName(), order.getAmount());
            
            return true;
        } catch (Exception e) {
            log.error("订单创建失败, orderId: {}, error: {}", order.getOrderId(), e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 查询订单是否存在
     * 用于事务消息回查时，检查本地事务是否执行成功
     * 
     * @param orderId 订单ID
     * @return 订单是否存在
     */
    public boolean orderExists(String orderId) {
        boolean exists = orderDatabase.containsKey(orderId);
        log.info("查询订单是否存在, orderId: {}, exists: {}", orderId, exists);
        return exists;
    }
    
    /**
     * 根据订单ID查询订单
     * 
     * @param orderId 订单ID
     * @return 订单对象
     */
    public Order getOrder(String orderId) {
        return orderDatabase.get(orderId);
    }
    
    /**
     * 获取所有订单（用于测试查看）
     * 
     * @return 所有订单
     */
    public Map<String, Order> getAllOrders() {
        return orderDatabase;
    }
}
