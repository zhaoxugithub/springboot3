package com.atguigu.ordermq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 顺序消息消费者
 *
 * 重要配置说明：
 * 1. consumeMode = ConsumeMode.ORDERLY - 设置为顺序消费模式
 * 2. 顺序消费保证同一队列的消息按照FIFO顺序消费
 * 3. 消费失败会阻塞当前队列，直到消费成功
 */
@Slf4j
@Component
@RocketMQMessageListener(
        topic = "order-topic",                      // 监听的Topic
        consumerGroup = "order-consumer-group",     // 消费者组
        consumeMode = ConsumeMode.ORDERLY          // 顺序消费模式（关键配置）
)
public class OrderedMessageConsumer implements RocketMQListener<String> {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 消费消息
     * 在顺序消费模式下，同一队列的消息会按顺序被消费
     *
     * @param message 接收到的消息
     */
    @Override
    public void onMessage(String message) {
        try {
            // 解析JSON消息
            OrderStep orderStep = objectMapper.readValue(message, OrderStep.class);

            // 模拟消息处理
            log.info("【顺序消费】收到消息 - 订单ID: {}, 步骤: {}, 线程: {}",
                    orderStep.getOrderId(),
                    orderStep.getDesc(),
                    Thread.currentThread().getName());

            // 模拟业务处理耗时
            Thread.sleep(100);

            log.info("【顺序消费】处理完成 - 订单ID: {}, 步骤: {}",
                    orderStep.getOrderId(),
                    orderStep.getDesc());

        } catch (Exception e) {
            log.error("顺序消息消费失败: {}", message, e);
            // 注意：顺序消费失败会阻塞当前队列，需要谨慎处理异常
        }
    }
}

