package com.atguigu.transactionMq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * 订单事务消息测试控制器
 * 
 * 提供REST API接口用于测试RocketMQ事务消息功能
 * 
 * 测试步骤：
 * 1. 启动RocketMQ NameServer和Broker
 * 2. 启动本应用
 * 3. 调用 POST /transaction/order/create 创建订单
 * 4. 观察日志，查看事务消息的执行流程
 * 5. 调用 GET /transaction/order/list 查看已创建的订单
 * 
 * @author atguigu
 */
@Slf4j
@RestController
@RequestMapping("/transaction/order")
public class TransactionMessageController {
    
    @Autowired
    private TransactionProducerService transactionProducerService;
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 创建订单（正常场景）
     * 
     * 场景说明：
     * 订单信息合法，本地事务执行成功，消息会被提交并被消费者消费
     * 
     * 测试命令：
     * curl -X POST "http://localhost:8080/transaction/order/create?userId=user001&productId=prod001&productName=iPhone15&amount=6999&quantity=1"
     * 
     * @param userId 用户ID
     * @param productId 商品ID
     * @param productName 商品名称
     * @param amount 订单金额
     * @param quantity 购买数量
     * @return 操作结果
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(
            @RequestParam(defaultValue = "user001") String userId,
            @RequestParam(defaultValue = "prod001") String productId,
            @RequestParam(defaultValue = "商品") String productName,
            @RequestParam(defaultValue = "100.00") BigDecimal amount,
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        try {
            // 生成订单ID
            String orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // 构建订单对象
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId(userId);
            order.setProductId(productId);
            order.setProductName(productName);
            order.setAmount(amount);
            order.setQuantity(quantity);
            order.setStatus(0); // 待支付
            order.setCreateTime(System.currentTimeMillis());
            
            log.info("接收到创建订单请求: orderId={}, userId={}, productName={}, amount={}, quantity={}", 
                    orderId, userId, productName, amount, quantity);
            
            // 发送事务消息
            boolean success = transactionProducerService.sendOrderTransactionMessage(order);
            
            if (success) {
                return Map.of(
                        "code", 200,
                        "message", "订单创建请求已提交",
                        "data", Map.of(
                                "orderId", orderId,
                                "userId", userId,
                                "productName", productName,
                                "amount", amount,
                                "quantity", quantity
                        )
                );
            } else {
                return Map.of(
                        "code", 500,
                        "message", "订单创建失败",
                        "data", null
                );
            }
            
        } catch (Exception e) {
            log.error("创建订单异常: {}", e.getMessage(), e);
            return Map.of(
                    "code", 500,
                    "message", "系统异常: " + e.getMessage(),
                    "data", null
            );
        }
    }
    
    /**
     * 创建订单（异常场景 - 金额为负数）
     * 
     * 场景说明：
     * 订单金额为负数，本地事务执行失败，消息会被回滚，消费者不会收到消息
     * 
     * 测试命令：
     * curl -X POST "http://localhost:8080/transaction/order/create-with-invalid-amount"
     * 
     * @return 操作结果
     */
    @PostMapping("/create-with-invalid-amount")
    public Map<String, Object> createOrderWithInvalidAmount() {
        try {
            String orderId = "ORDER-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            
            // 构建订单对象 - 故意设置金额为负数
            Order order = new Order();
            order.setOrderId(orderId);
            order.setUserId("user002");
            order.setProductId("prod002");
            order.setProductName("测试商品-负数金额");
            order.setAmount(new BigDecimal("-100.00")); // 负数金额，会导致本地事务失败
            order.setQuantity(1);
            order.setStatus(0);
            order.setCreateTime(System.currentTimeMillis());
            
            log.info("测试异常场景：创建金额为负数的订单, orderId={}", orderId);
            
            transactionProducerService.sendOrderTransactionMessage(order);
            
            return Map.of(
                    "code", 200,
                    "message", "测试异常场景：订单金额为负数，本地事务会失败并回滚消息",
                    "data", Map.of("orderId", orderId, "note", "查看日志，消息应该被回滚，消费者不会收到")
            );
            
        } catch (Exception e) {
            log.error("异常场景测试失败: {}", e.getMessage(), e);
            return Map.of(
                    "code", 500,
                    "message", "系统异常: " + e.getMessage(),
                    "data", null
            );
        }
    }
    
    /**
     * 查询指定订单
     * 
     * 测试命令：
     * curl "http://localhost:8080/transaction/order/get?orderId=ORDER-12345678"
     * 
     * @param orderId 订单ID
     * @return 订单信息
     */
    @GetMapping("/get")
    public Map<String, Object> getOrder(@RequestParam String orderId) {
        Order order = orderService.getOrder(orderId);
        if (order != null) {
            return Map.of(
                    "code", 200,
                    "message", "查询成功",
                    "data", order
            );
        } else {
            return Map.of(
                    "code", 404,
                    "message", "订单不存在",
                    "data", null
            );
        }
    }
    
    /**
     * 查询所有订单
     * 
     * 用于查看所有已创建的订单，验证事务消息是否成功
     * 
     * 测试命令：
     * curl "http://localhost:8080/transaction/order/list"
     * 
     * @return 所有订单列表
     */
    @GetMapping("/list")
    public Map<String, Object> listOrders() {
        Map<String, Order> orders = orderService.getAllOrders();
        return Map.of(
                "code", 200,
                "message", "查询成功",
                "data", orders,
                "total", orders.size()
        );
    }
}
