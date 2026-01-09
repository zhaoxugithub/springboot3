package com.atguigu.transactionMq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

/**
 * 订单事务消息生产者服务
 * 
 * 事务消息使用场景：
 * 确保本地事务和消息发送的一致性，解决分布式事务问题
 * 
 * 典型应用场景：
 * 1. 电商订单创建：订单创建成功后，通知库存系统扣减库存、通知积分系统增加积分
 * 2. 账户转账：转账成功后，发送通知消息
 * 3. 数据同步：主数据变更后，同步到其他系统
 * 
 * @author atguigu
 */
@Slf4j
@Service
public class TransactionProducerService {
    
    /**
     * RocketMQ事务消息发送的Topic
     */
    private static final String TRANSACTION_TOPIC = "order-transaction-topic";
    
    /**
     * 消息标签，用于消息过滤
     */
    private static final String TAG = "order-create";
    
    @Autowired
    private RocketMQTemplate rocketMQTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 发送事务消息
     * 
     * 事务消息发送流程：
     * 1. 发送半消息（Half Message）到Broker
     * 2. Broker存储半消息，此时消费者不可见
     * 3. 执行本地事务（executeLocalTransaction）
     * 4. 根据本地事务结果，提交或回滚消息
     * 5. 如果提交，消费者可以消费消息；如果回滚，消息被删除
     * 
     * @param order 订单对象
     * @return 消息发送结果
     */
    public boolean sendOrderTransactionMessage(Order order) {
        try {
            // 将订单对象转换为JSON字符串
            String orderJson = objectMapper.writeValueAsString(order);
            
            // 构建消息，设置消息的Key为订单ID，便于消息追踪和去重
            Message<String> message = MessageBuilder
                    .withPayload(orderJson)
                    .setHeader("KEYS", order.getOrderId())
                    .build();
            
            // 目标地址：Topic:Tag
            String destination = TRANSACTION_TOPIC + ":" + TAG;
            
            log.info("准备发送事务消息, destination: {}, orderId: {}", destination, order.getOrderId());
            
            /**
             * 发送事务消息
             * 参数说明：
             * 1. destination: 消息目的地（Topic:Tag）
             * 2. message: 消息体
             * 3. arg: 业务参数，会传递给executeLocalTransaction方法，可用于业务扩展
             * 
             * 注意：sendMessageInTransaction是同步方法，会等待半消息发送成功和本地事务执行完成
             */
            rocketMQTemplate.sendMessageInTransaction(destination, message, order);
            
            log.info("事务消息发送完成, orderId: {}", order.getOrderId());
            return true;
            
        } catch (Exception e) {
            log.error("发送事务消息失败, orderId: {}, error: {}", order.getOrderId(), e.getMessage(), e);
            return false;
        }
    }
}
