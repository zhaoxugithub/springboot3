package com.atguigu.rocketmq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RocketMQMessageListener(topic = "${rocketmq.consumer.topic}", consumerGroup = "${rocketmq.consumer.group}", consumeMode = org.apache.rocketmq.spring.annotation.ConsumeMode.CONCURRENTLY)
public class SampleConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String message) {
        log.info("Received message: {}", message);
    }
}

