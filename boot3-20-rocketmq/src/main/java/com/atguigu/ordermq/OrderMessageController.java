package com.atguigu.ordermq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单消息测试控制器
 * 提供HTTP接口来测试顺序消息和非顺序消息的发送
 */
@Slf4j
@RestController
@RequestMapping("/order")
public class OrderMessageController {

    @Autowired
    private OrderedMessageProducer orderedMessageProducer;

    @Autowired
    private NonOrderedMessageProducer nonOrderedMessageProducer;

    /**
     * 测试顺序消息
     * 访问: http://localhost:8080/order/sendOrderedMessage
     * <p>
     * 预期效果：
     * 1. 同一订单的消息会发送到同一个队列
     * 2. 消费者会按照发送顺序消费消息
     * 3. 日志中可以看到同一订单的步骤是按顺序执行的：创建订单 -> 付款 -> 推送 -> 完成
     */
    @GetMapping("/sendOrderedMessage")
    public String sendOrderedMessage() {
        log.info("========== 开始发送顺序消息 ==========");
        orderedMessageProducer.sendOrderedMessage();
        return "顺序消息发送成功！请查看控制台日志观察消息的发送和消费顺序。";
    }

    /**
     * 测试非顺序消息
     * 访问: http://localhost:8080/order/sendNonOrderedMessage
     * <p>
     * 预期效果：
     * 1. 消息会被随机分配到不同的队列
     * 2. 消费者会并发消费消息，不保证顺序
     * 3. 日志中可以看到同一订单的步骤可能是乱序的
     */
    @GetMapping("/sendNonOrderedMessage")
    public String sendNonOrderedMessage() {
        log.info("========== 开始发送非顺序消息 ==========");
        nonOrderedMessageProducer.sendNonOrderedMessage();
        return "非顺序消息发送成功！请查看控制台日志观察消息的发送和消费顺序。";
    }

    /**
     * 对比测试：同时发送顺序消息和非顺序消息
     * 访问: http://localhost:8080/order/compare
     * <p>
     * 用于对比两种消息模式的区别
     */
    @GetMapping("/compare")
    public String compare() {
        log.info("========== 开始对比测试 ==========");

        log.info("---------- 发送顺序消息 ----------");
        orderedMessageProducer.sendOrderedMessage();
        try {
            Thread.sleep(2000); // 等待2秒，让顺序消息处理完
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.info("---------- 发送非顺序消息 ----------");
        nonOrderedMessageProducer.sendNonOrderedMessage();
        return "对比测试完成！请查看控制台日志对比两种消息模式的区别。\n" + "顺序消息：同一订单的步骤会按顺序执行\n" + "非顺序消息：同一订单的步骤可能乱序执行";
    }
}

