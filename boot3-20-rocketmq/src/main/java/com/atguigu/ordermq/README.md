# RocketMQ 顺序消息与非顺序消息示例

## 📁 项目结构

```
ordermq/
├── OrderStep.java                    # 订单步骤实体类
├── OrderedMessageProducer.java       # 顺序消息生产者
├── OrderedMessageConsumer.java       # 顺序消息消费者
├── NonOrderedMessageProducer.java    # 非顺序消息生产者
├── NonOrderedMessageConsumer.java    # 非顺序消息消费者
└── OrderMessageController.java       # 测试控制器
```

## 🎯 功能说明

### 1. 顺序消息（Ordered Message）

**核心特点：**
- 使用 `syncSendOrderly()` 方法发送消息
- 使用 `orderId` 作为 hashKey，确保同一订单的消息发送到同一个队列
- 消费者使用 `ConsumeMode.ORDERLY` 模式，保证按顺序消费
- 适用场景：订单流程、状态机流转等需要严格保证顺序的场景

**实现类：**
- `OrderedMessageProducer.java` - 生产者
- `OrderedMessageConsumer.java` - 消费者

**关键代码：**
```java
// 生产者：使用orderId作为hashKey
rocketMQTemplate.syncSendOrderly("order-topic", message, String.valueOf(orderId));

// 消费者：设置顺序消费模式
@RocketMQMessageListener(
    topic = "order-topic",
    consumerGroup = "order-consumer-group",
    consumeMode = ConsumeMode.ORDERLY  // 关键配置
)
```

### 2. 非顺序消息（Non-Ordered Message）

**核心特点：**
- 使用 `syncSend()` 方法发送消息
- 消息随机分配到不同队列
- 消费者使用 `ConsumeMode.CONCURRENTLY` 模式，并发消费
- 吞吐量高，适用于不需要保证顺序的场景

**实现类：**
- `NonOrderedMessageProducer.java` - 生产者
- `NonOrderedMessageConsumer.java` - 消费者

**关键代码：**
```java
// 生产者：普通发送
rocketMQTemplate.syncSend("non-order-topic", message);

// 消费者：并发消费模式（默认）
@RocketMQMessageListener(
    topic = "non-order-topic",
    consumerGroup = "non-order-consumer-group",
    consumeMode = ConsumeMode.CONCURRENTLY  // 可以省略，默认就是并发模式
)
```

## 🚀 使用方法

### 1. 启动应用

确保 RocketMQ 服务已启动，然后运行 Spring Boot 应用：

```bash
mvn spring-boot:run
```

### 2. 测试顺序消息

访问：http://localhost:8080/order/sendOrderedMessage

**预期效果：**
- 同一订单的消息会发送到同一个队列
- 消费顺序与发送顺序一致
- 日志示例：
  ```
  订单1001: 创建订单 -> 付款 -> 推送 -> 完成
  订单1002: 创建订单 -> 付款 -> 推送 -> 完成
  订单1003: 创建订单 -> 付款 -> 推送 -> 完成
  ```

### 3. 测试非顺序消息

访问：http://localhost:8080/order/sendNonOrderedMessage

**预期效果：**
- 消息会被随机分配到不同队列
- 消费顺序可能与发送顺序不一致
- 日志示例（可能是乱序的）：
  ```
  订单2001: 创建订单
  订单2002: 创建订单
  订单2001: 推送
  订单2003: 付款
  订单2002: 完成
  ...
  ```

### 4. 对比测试

访问：http://localhost:8080/order/compare

同时测试两种消息模式，观察对比效果。

## 📊 两种模式对比

| 特性 | 顺序消息 | 非顺序消息 |
|------|---------|-----------|
| 发送方法 | `syncSendOrderly()` | `syncSend()` |
| 消费模式 | `ORDERLY` | `CONCURRENTLY` |
| 队列选择 | 基于hashKey路由到固定队列 | 随机分配到不同队列 |
| 消费顺序 | 保证顺序 | 不保证顺序 |
| 并发度 | 同一队列串行消费 | 多线程并发消费 |
| 吞吐量 | 较低 | 较高 |
| 适用场景 | 订单流程、状态流转 | 日志记录、通知推送 |
| 失败处理 | 阻塞当前队列直到成功 | 不阻塞其他消息，自动重试 |

## ⚠️ 注意事项

### 顺序消息注意事项：

1. **队列数量限制**：同一hashKey的消息会路由到同一个队列，队列数量会影响并发度
2. **消费失败处理**：顺序消费失败会阻塞该队列，需要谨慎处理异常
3. **性能权衡**：顺序消息的吞吐量低于普通消息，只在必要时使用

### 非顺序消息注意事项：

1. **幂等性**：由于可能重复消费，消费端需要保证幂等性
2. **重试机制**：消费失败会自动重试，需要控制重试次数
3. **并发控制**：高并发场景下注意资源竞争问题

## 🔧 配置说明

在 `application.yml` 中配置 RocketMQ：

```yaml
rocketmq:
  name-server: 150.158.27.19:9876  # RocketMQ NameServer地址
  producer:
    group: my-producer-group        # 生产者组
  consumer:
    group: my-consumer-group        # 消费者组（会被监听器中的group覆盖）
```

## 📝 核心知识点

### 1. 顺序消息实现原理

- **生产端**：通过 MessageQueueSelector 选择队列，相同 hashKey 路由到同一队列
- **Broker端**：使用队列级别的锁保证顺序存储
- **消费端**：使用队列级别的锁保证顺序消费

### 2. 队列选择算法

```java
// RocketMQ内部实现（简化）
int queueId = Math.abs(hashKey.hashCode()) % queueCount;
```

### 3. 顺序消费的限制

- 消费失败会重试，期间会阻塞该队列的后续消息
- 不能跳过失败的消息继续消费后面的消息
- 需要保证消费逻辑的幂等性和快速失败

## 🎓 学习建议

1. 先运行顺序消息测试，观察日志中的队列ID和消费顺序
2. 再运行非顺序消息测试，对比消费顺序的差异
3. 使用 `/compare` 接口进行直观对比
4. 查看生产者和消费者的日志，理解消息的流转过程

## 📚 相关文档

- [RocketMQ官方文档](https://rocketmq.apache.org/)
- [RocketMQ Spring Boot Starter](https://github.com/apache/rocketmq-spring)

