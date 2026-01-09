package com.atguigu.transactionMq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

/**
 * RocketMQ事务消息监听器
 * 
 * 事务消息的处理流程：
 * 1. 生产者发送半消息（Half Message）到Broker
 * 2. Broker返回半消息发送成功的响应
 * 3. 生产者执行本地事务（executeLocalTransaction方法）
 * 4. 根据本地事务执行结果，返回COMMIT、ROLLBACK或UNKNOWN
 * 5. 如果返回COMMIT，Broker将消息投递给消费者
 * 6. 如果返回ROLLBACK，Broker将删除该消息
 * 7. 如果返回UNKNOWN或超时未响应，Broker会回查生产者（checkLocalTransaction方法）
 * 
 * @author atguigu
 */
@Slf4j
@RocketMQTransactionListener(rocketMQTemplateBeanName = "rocketMQTemplate")
public class OrderTransactionListener implements RocketMQLocalTransactionListener {
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * 执行本地事务
     * 
     * 当半消息发送成功后，RocketMQ会回调此方法执行本地事务
     * 本例中，我们在这里执行订单创建的数据库操作
     * 
     * @param message 消息对象，包含订单信息
     * @param arg 业务参数，由sendMessageInTransaction方法传入
     * @return 本地事务执行状态
     *         COMMIT - 提交事务，消息将被消费者消费
     *         ROLLBACK - 回滚事务，消息将被删除
     *         UNKNOWN - 未知状态，Broker会进行回查
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        try {
            // 从消息中获取订单信息
            String orderJson = new String((byte[]) message.getPayload());
            Order order = objectMapper.readValue(orderJson, Order.class);
            
            log.info("开始执行本地事务, orderId: {}", order.getOrderId());
            
            // 执行本地事务：创建订单
            boolean success = orderService.createOrder(order);
            
            if (success) {
                // 本地事务执行成功，提交消息
                log.info("本地事务执行成功，提交事务消息, orderId: {}", order.getOrderId());
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                // 本地事务执行失败，回滚消息
                log.warn("本地事务执行失败，回滚事务消息, orderId: {}", order.getOrderId());
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            
        } catch (Exception e) {
            // 发生异常，返回UNKNOWN状态，等待Broker回查
            log.error("执行本地事务发生异常，返回UNKNOWN状态等待回查", e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
    
    /**
     * 回查本地事务状态
     * 
     * 当executeLocalTransaction返回UNKNOWN状态，或者长时间未返回结果时，
     * RocketMQ Broker会定期回查事务状态（默认间隔60秒，最多回查15次）
     * 
     * 回查的目的是确认本地事务最终是成功还是失败，以决定消息是提交还是回滚
     * 
     * @param message 消息对象
     * @return 本地事务状态
     *         COMMIT - 本地事务已成功，提交消息
     *         ROLLBACK - 本地事务已失败，删除消息
     *         UNKNOWN - 仍无法确定，继续回查（不建议一直返回UNKNOWN）
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        try {
            // 从消息中获取订单ID
            String orderJson = new String((byte[]) message.getPayload());
            Order order = objectMapper.readValue(orderJson, Order.class);
            String orderId = order.getOrderId();
            
            log.info("Broker回查本地事务状态, orderId: {}", orderId);
            
            // 查询数据库，检查订单是否已创建
            boolean orderExists = orderService.orderExists(orderId);
            
            if (orderExists) {
                // 订单已创建，说明本地事务执行成功，提交消息
                log.info("回查结果：订单已创建，提交事务消息, orderId: {}", orderId);
                return RocketMQLocalTransactionState.COMMIT;
            } else {
                // 订单不存在，说明本地事务执行失败或未执行，回滚消息
                log.info("回查结果：订单不存在，回滚事务消息, orderId: {}", orderId);
                return RocketMQLocalTransactionState.ROLLBACK;
            }
            
        } catch (Exception e) {
            // 回查异常，返回UNKNOWN继续回查
            // 注意：不要一直返回UNKNOWN，应该有明确的业务判断逻辑
            log.error("回查本地事务状态发生异常", e);
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
