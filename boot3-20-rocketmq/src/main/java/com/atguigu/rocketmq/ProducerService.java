package com.atguigu.rocketmq;

import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
public class ProducerService {

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Value("${rocketmq.consumer.topic:test1}")
    private String topic;

    public void send(String tag, String payload) {
        String destination = topic + ":" + tag;
        rocketMQTemplate.convertAndSend(destination, payload);
    }

    public void sendWithKey(String tag, String key, String payload) {
        String destination = topic + ":" + tag;
        rocketMQTemplate.syncSend(destination, MessageBuilder.withPayload(payload).setHeader("KEYS", key).build());
    }
}

