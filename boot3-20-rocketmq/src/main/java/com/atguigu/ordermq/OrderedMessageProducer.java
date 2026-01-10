package com.atguigu.ordermq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 顺序消息生产者
 * 顺序消息的特点：
 * 1. 同一个订单的消息会发送到同一个队列
 * 2. 消费者按照消息发送的顺序进行消费
 * 3. 适用于需要严格保证顺序的场景，如订单流程：创建->付款->推送->完成
 */
@Slf4j
@Component
public class OrderedMessageProducer {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 构建订单步骤列表（模拟数据）
     * @return 订单步骤列表
     */
    public List<OrderStep> buildOrders() {
        List<OrderStep> orderList = new ArrayList<>();

        // 订单1001的4个步骤
        orderList.add(new OrderStep(1001L, "创建订单"));
        orderList.add(new OrderStep(1001L, "付款"));
        orderList.add(new OrderStep(1001L, "推送"));
        orderList.add(new OrderStep(1001L, "完成"));

        // 订单1002的4个步骤
        orderList.add(new OrderStep(1002L, "创建订单"));
        orderList.add(new OrderStep(1002L, "付款"));
        orderList.add(new OrderStep(1002L, "推送"));
        orderList.add(new OrderStep(1002L, "完成"));

        // 订单1003的4个步骤
        orderList.add(new OrderStep(1003L, "创建订单"));
        orderList.add(new OrderStep(1003L, "付款"));
        orderList.add(new OrderStep(1003L, "推送"));
        orderList.add(new OrderStep(1003L, "完成"));

        return orderList;
    }

    /**
     * 发送顺序消息
     * 使用 orderId 作为 hashKey，确保同一订单的消息发送到同一个队列
     */
    public void sendOrderedMessage() {
        try {
            List<OrderStep> orderSteps = buildOrders();

            for (OrderStep orderStep : orderSteps) {
                // 将订单步骤转换为JSON字符串
                String jsonString = objectMapper.writeValueAsString(orderStep);

                // 构建消息
                Message<String> message = MessageBuilder.withPayload(jsonString).build();

                /**
                 * syncSendOrderly 方法说明：
                 * 参数1: destination - Topic名称
                 * 参数2: message - 消息内容
                 * 参数3: hashKey - 用于选择队列的key（相同key的消息会发送到同一个队列）
                 *
                 * 关键点：使用orderId作为hashKey，保证同一订单的所有消息都发送到同一个队列
                 */
                SendResult sendResult = rocketMQTemplate.syncSendOrderly(
                        "order-topic",
                        message,
                        String.valueOf(orderStep.getOrderId())  // 使用订单ID作为hashKey
                );

                log.info("顺序消息发送成功 - 订单ID: {}, 步骤: {}, 队列ID: {}, 发送状态: {}",
                        orderStep.getOrderId(),
                        orderStep.getDesc(),
                        sendResult.getMessageQueue().getQueueId(),
                        sendResult.getSendStatus());
            }
        } catch (Exception e) {
            log.error("发送顺序消息失败", e);
        }
    }
}

