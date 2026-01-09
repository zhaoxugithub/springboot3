package com.atguigu.transactionMq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 订单事务消息消费者
 * 
 * 职责说明：
 * 1. 监听订单事务消息
 * 2. 处理下游业务逻辑，如：
 *    - 扣减库存
 *    - 增加用户积分
 *    - 发送订单通知
 *    - 记录订单日志
 * 
 * 消费特点：
 * - 只有当生产者本地事务提交（COMMIT）后，消费者才能收到消息
 * - 如果生产者本地事务回滚（ROLLBACK），消费者永远不会收到消息
 * - 保证了本地事务和消息发送的最终一致性
 * 
 * @author atguigu
 */
@Slf4j
@Service
@RocketMQMessageListener(
        topic = "order-transaction-topic",           // 监听的Topic
        consumerGroup = "order-transaction-consumer", // 消费者组
        consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY // 并发消费模式
)
public class OrderTransactionConsumer implements RocketMQListener<String> {
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 消费消息
     * 
     * 业务场景：
     * 当订单创建成功后（本地事务已提交），消费者收到消息，执行后续业务逻辑
     * 
     * @param message 消息内容（订单JSON字符串）
     */
    @Override
    public void onMessage(String message) {
        try {
            log.info("========== 开始消费事务消息 ==========");
            log.info("接收到消息: {}", message);
            
            // 解析订单信息
            Order order = objectMapper.readValue(message, Order.class);
            
            // ============ 模拟下游业务处理 ============
            
            // 1. 扣减库存
            deductInventory(order);
            
            // 2. 增加用户积分
            addUserPoints(order);
            
            // 3. 发送订单通知
            sendOrderNotification(order);
            
            // 4. 记录订单日志
            recordOrderLog(order);
            
            log.info("========== 事务消息消费完成 ==========");
            log.info("订单ID: {}, 用户ID: {}, 商品: {}, 金额: {}, 数量: {}", 
                    order.getOrderId(), order.getUserId(), order.getProductName(), 
                    order.getAmount(), order.getQuantity());
            
        } catch (Exception e) {
            // 消费失败处理
            // RocketMQ会根据重试策略自动重试
            // 如果多次重试后仍然失败，消息会进入死信队列
            log.error("消费事务消息失败: {}", e.getMessage(), e);
            throw new RuntimeException("消费消息失败", e);
        }
    }
    
    /**
     * 扣减库存
     * 实际场景：调用库存系统API扣减商品库存
     * 
     * @param order 订单信息
     */
    private void deductInventory(Order order) {
        log.info(">>> 扣减库存：商品ID={}, 扣减数量={}", order.getProductId(), order.getQuantity());
        // 实际开发中，这里应该调用库存系统的接口
        // inventoryService.deduct(order.getProductId(), order.getQuantity());
        
        // 模拟库存扣减耗时操作
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info(">>> 库存扣减成功");
    }
    
    /**
     * 增加用户积分
     * 实际场景：根据订单金额计算并增加用户积分
     * 
     * @param order 订单信息
     */
    private void addUserPoints(Order order) {
        // 计算积分：每消费1元获得1积分
        int points = order.getAmount().intValue();
        log.info(">>> 增加用户积分：用户ID={}, 增加积分={}", order.getUserId(), points);
        // 实际开发中，这里应该调用积分系统的接口
        // pointsService.add(order.getUserId(), points);
        
        log.info(">>> 积分增加成功");
    }
    
    /**
     * 发送订单通知
     * 实际场景：通过短信、邮件、站内信等方式通知用户订单创建成功
     * 
     * @param order 订单信息
     */
    private void sendOrderNotification(Order order) {
        log.info(">>> 发送订单通知：用户ID={}, 订单ID={}", order.getUserId(), order.getOrderId());
        // 实际开发中，这里应该调用通知服务的接口
        // notificationService.sendOrderCreatedNotification(order.getUserId(), order.getOrderId());
        
        log.info(">>> 订单通知发送成功");
    }
    
    /**
     * 记录订单日志
     * 实际场景：将订单操作记录到日志系统或数据仓库，用于数据分析
     * 
     * @param order 订单信息
     */
    private void recordOrderLog(Order order) {
        log.info(">>> 记录订单日志：订单ID={}, 操作=创建订单", order.getOrderId());
        // 实际开发中，这里应该调用日志服务的接口
        // logService.recordOrderOperation(order, "CREATE");
        
        log.info(">>> 订单日志记录成功");
    }
}
