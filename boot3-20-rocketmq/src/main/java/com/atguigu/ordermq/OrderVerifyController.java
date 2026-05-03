package com.atguigu.ordermq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 顺序消息验证控制器
 * 用于验证和演示顺序消息的正确性
 */
@Slf4j
@RestController
@RequestMapping("/order/verify")
public class OrderVerifyController {

    @Autowired
    private OrderedMessageProducer orderedMessageProducer;

    /**
     * 验证单个订单的顺序消费
     * 访问: http://localhost:8080/order/verify/singleOrder?orderId=1001
     *
     * 这个接口只发送一个订单的消息，便于观察该订单的消费顺序
     */
    @GetMapping("/singleOrder")
    public String verifySingleOrder(@RequestParam(defaultValue = "1001") Long orderId) {
        log.info("========== 验证单个订单的顺序消费 ==========");
        log.info("发送订单 {} 的4个步骤消息", orderId);

        orderedMessageProducer.sendSingleOrderMessage(orderId);

        return String.format("订单 %d 的消息已发送！\n" +
                "请观察控制台日志，消费顺序应该是：\n" +
                "1. 创建订单\n" +
                "2. 付款\n" +
                "3. 推送\n" +
                "4. 完成\n" +
                "注意：所有步骤会发送到同一个队列ID", orderId);
    }

    /**
     * 验证多个订单的顺序消费
     * 访问: http://localhost:8080/order/verify/multipleOrders
     *
     * 发送3个订单的消息，观察不同订单之间的并行处理和单个订单内部的顺序性
     */
    @GetMapping("/multipleOrders")
    public String verifyMultipleOrders() {
        log.info("========== 验证多个订单的顺序消费 ==========");
        log.info("发送3个订单的消息，每个订单4个步骤");

        orderedMessageProducer.sendOrderedMessage();

        return "3个订单的消息已发送！\n" +
                "请观察控制台日志：\n" +
                "1. 同一订单的消息会发送到同一个队列ID\n" +
                "2. 同一订单内部的步骤会按顺序消费：创建订单 -> 付款 -> 推送 -> 完成\n" +
                "3. 不同订单之间可能并行处理（如果分配到不同队列）\n" +
                "4. 注意观察队列ID和消费顺序的对应关系";
    }

    /**
     * 清空消费日志视图（实际上是打印分隔符）
     * 访问: http://localhost:8080/order/verify/clear
     */
    @GetMapping("/clear")
    public String clearLogs() {
        log.info("\n\n");
        log.info("=".repeat(80));
        log.info("=".repeat(80));
        log.info("==================== 日志分隔线 - 开始新的测试 ====================");
        log.info("=".repeat(80));
        log.info("=".repeat(80));
        log.info("\n\n");
        return "日志分隔符已打印，方便区分不同测试";
    }

    /**
     * 查看顺序消费的说明
     * 访问: http://localhost:8080/order/verify/help
     */
    @GetMapping("/help")
    public String showHelp() {
        return """
                ========== 顺序消息验证说明 ==========
                
                1. 测试单个订单：
                   GET /order/verify/singleOrder?orderId=1001
                   只发送一个订单的消息，便于观察顺序
                
                2. 测试多个订单：
                   GET /order/verify/multipleOrders
                   发送3个订单的消息，观察并行和顺序的关系
                
                3. 清空日志视图：
                   GET /order/verify/clear
                   在日志中打印分隔符，便于区分不同测试
                
                ========== 如何验证顺序消费 ==========
                
                ✅ 正确的顺序消费表现：
                   - 同一订单的所有消息发送到同一个队列ID
                   - 消费日志显示：创建订单 -> 付款 -> 推送 -> 完成（严格按顺序）
                   - 每个步骤的"开始处理"和"处理完成"成对出现
                
                ❌ 如果出现乱序：
                   - 检查是否有多个消费者实例在运行
                   - 检查消费者配置中的 consumeMode 是否为 ORDERLY
                   - 检查生产者是否正确使用了 syncSendOrderly 并传入 hashKey
                
                ========== 关键配置 ==========
                
                生产者：
                   rocketMQTemplate.syncSendOrderly(topic, message, orderId);
                   ↑ 使用 orderId 作为 hashKey
                
                消费者：
                   @RocketMQMessageListener(
                       consumeMode = ConsumeMode.ORDERLY,  ← 关键配置
                       consumeThreadMax = 1                ← 单线程消费
                   )
                
                ========================================
                """;
    }
}

