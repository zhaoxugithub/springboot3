package com.atguigu.ordermq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 非顺序消息消费者（并发消费）
 *
 * 重要配置说明：
 * 1. consumeMode = ConsumeMode.CONCURRENTLY - 设置为并发消费模式（默认模式）
 * 2. 并发消费不保证消息的消费顺序
 * 3. 多个线程并发消费，吞吐量高
 * 4. 消费失败可以重试，不会阻塞其他消息的消费
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "non-order-topic",                     // 监听的Topic
        consumerGroup = "non-order-consumer-group",    // 消费者组
        consumeMode = ConsumeMode.CONCURRENTLY         // 并发消费模式（默认，可以不写）
)
public class NonOrderedMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 消费消息
     * 在并发消费模式下，消息会被多个线程并发消费，不保证顺序
     *
     * @param message 接收到的消息
     */
    @Override
    public void onMessage(String message) {
        try {
            // 解析JSON消息
            OrderStep orderStep = objectMapper.readValue(message, OrderStep.class);

            // 模拟消息处理
            log.info("【并发消费】收到消息 - 订单ID: {}, 步骤: {}, 线程: {}",
                    orderStep.getOrderId(),
                    orderStep.getDesc(),
                    Thread.currentThread().getName());

            // 模拟业务处理耗时
            Thread.sleep(100);

            log.info("【并发消费】处理完成 - 订单ID: {}, 步骤: {}",
                    orderStep.getOrderId(),
                    orderStep.getDesc());

        } catch (Exception e) {
            log.error("非顺序消息消费失败: {}", message, e);
            // 注意：并发消费失败会自动重试，不会阻塞其他消息
            throw new RuntimeException("消费失败，触发重试", e);
        }
    }
}

