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
 * 非顺序消息生产者（普通消息）
 * 非顺序消息的特点：
 * 1. 消息会被随机分配到不同的队列
 * 2. 消费者并发消费，不保证消费顺序
 * 3. 吞吐量高，适用于不需要保证顺序的场景
 */
@Slf4j
@Component
public class NonOrderedMessageProducer {

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

        // 订单2001的4个步骤
        orderList.add(new OrderStep(2001L, "创建订单"));
        orderList.add(new OrderStep(2001L, "付款"));
        orderList.add(new OrderStep(2001L, "推送"));
        orderList.add(new OrderStep(2001L, "完成"));

        // 订单2002的4个步骤
        orderList.add(new OrderStep(2002L, "创建订单"));
        orderList.add(new OrderStep(2002L, "付款"));
        orderList.add(new OrderStep(2002L, "推送"));
        orderList.add(new OrderStep(2002L, "完成"));

        // 订单2003的4个步骤
        orderList.add(new OrderStep(2003L, "创建订单"));
        orderList.add(new OrderStep(2003L, "付款"));
        orderList.add(new OrderStep(2003L, "推送"));
        orderList.add(new OrderStep(2003L, "完成"));

        return orderList;
    }

    /**
     * 发送非顺序消息（普通消息）
     * 使用 syncSend 方法，消息会被随机分配到不同的队列
     */
    public void sendNonOrderedMessage() {
        try {
            List<OrderStep> orderSteps = buildOrders();

            for (OrderStep orderStep : orderSteps) {
                // 将订单步骤转换为JSON字符串
                String jsonString = objectMapper.writeValueAsString(orderStep);

                // 构建消息
                Message<String> message = MessageBuilder.withPayload(jsonString).build();

                /**
                 * syncSend 方法说明：
                 * 参数1: destination - Topic名称
                 * 参数2: message - 消息内容
                 *
                 * 特点：消息会被轮询或随机分配到不同的队列，不保证顺序
                 */
                SendResult sendResult = rocketMQTemplate.syncSend(
                        "non-order-topic",
                        message
                );

                log.info("非顺序消息发送成功 - 订单ID: {}, 步骤: {}, 队列ID: {}, 发送状态: {}",
                        orderStep.getOrderId(),
                        orderStep.getDesc(),
                        sendResult.getMessageQueue().getQueueId(),
                        sendResult.getSendStatus());
            }
        } catch (Exception e) {
            log.error("发送非顺序消息失败", e);
        }
    }
}

