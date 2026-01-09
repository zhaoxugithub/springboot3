# RocketMQ 事务消息示例

## 简介

本示例演示了如何使用 RocketMQ 事务消息来保证分布式事务的最终一致性。

## 业务场景

电商订单创建场景：
1. 用户创建订单，系统需要在本地数据库插入订单记录
2. 订单创建成功后，需要通知下游系统（库存系统、积分系统、通知系统等）
3. 要求：本地订单创建和消息发送必须保持一致性

使用事务消息可以确保：
- 如果订单创建成功，消息一定会被发送并被下游系统消费
- 如果订单创建失败，消息一定不会被发送
- 避免了订单创建成功但消息发送失败的不一致情况

## 核心类说明

### 1. Order.java
订单实体类，包含订单的基本信息。

### 2. OrderService.java
订单服务类，模拟本地数据库操作：
- `createOrder()`: 创建订单（本地事务）
- `orderExists()`: 查询订单是否存在（用于事务回查）

### 3. OrderTransactionListener.java
RocketMQ 事务消息监听器，核心类：
- `executeLocalTransaction()`: 执行本地事务，返回 COMMIT/ROLLBACK/UNKNOWN
- `checkLocalTransaction()`: 事务回查，当本地事务状态不明确时，Broker 会回调此方法

### 4. TransactionProducerService.java
事务消息生产者服务：
- `sendOrderTransactionMessage()`: 发送事务消息

### 5. OrderTransactionConsumer.java
事务消息消费者：
- 监听订单事务消息
- 执行下游业务逻辑（扣减库存、增加积分、发送通知等）

### 6. TransactionMessageController.java
REST API 控制器，提供测试接口：
- `POST /transaction/order/create`: 创建订单（正常场景）
- `POST /transaction/order/create-with-invalid-amount`: 创建订单（异常场景）
- `GET /transaction/order/get`: 查询订单
- `GET /transaction/order/list`: 查询所有订单

## 事务消息执行流程

```
生产者                    Broker                    消费者
  |                         |                          |
  |--1. 发送半消息--------->|                          |
  |                         |--存储半消息（不可消费）   |
  |<--2. 半消息发送成功-----|                          |
  |                         |                          |
  |--3. 执行本地事务        |                          |
  |   (executeLocalTransaction)                       |
  |                         |                          |
  |--4. 提交/回滚消息------>|                          |
  |                         |                          |
  |                         |--5. 如果提交------------>|
  |                         |   消费者可以消费消息     |
  |                         |                          |
  |                         |   如果回滚，删除消息     |
  |                         |                          |
  |                         |--6. 如果超时或UNKNOWN    |
  |<--7. 回查本地事务-------|   定期回查              |
  |   (checkLocalTransaction)                         |
  |                         |                          |
  |--8. 返回事务状态------->|                          |
```

## 如何测试

### 前置条件
1. 确保 RocketMQ NameServer 和 Broker 已启动
2. 确保 application.yml 中的 RocketMQ 配置正确

### 测试步骤

#### 1. 正常场景测试（本地事务成功）
```bash
# 创建订单
curl -X POST "http://localhost:8080/transaction/order/create?userId=user001&productId=prod001&productName=iPhone15&amount=6999&quantity=1"

# 预期结果：
# - 订单创建成功
# - 事务消息被提交
# - 消费者收到消息并执行下游业务逻辑
```

#### 2. 异常场景测试（本地事务失败）
```bash
# 创建金额为负数的订单
curl -X POST "http://localhost:8080/transaction/order/create-with-invalid-amount"

# 预期结果：
# - 订单创建失败（金额校验不通过）
# - 事务消息被回滚
# - 消费者不会收到消息
```

#### 3. 查询订单
```bash
# 查询所有订单
curl "http://localhost:8080/transaction/order/list"

# 查询指定订单
curl "http://localhost:8080/transaction/order/get?orderId=ORDER-12345678"
```

### 观察日志

启动应用后，执行上述测试，观察控制台日志：

**正常场景日志关键点：**
```
接收到创建订单请求...
准备发送事务消息...
开始执行本地事务...
订单创建成功...
本地事务执行成功，提交事务消息...
========== 开始消费事务消息 ==========
接收到消息...
>>> 扣减库存...
>>> 增加用户积分...
>>> 发送订单通知...
>>> 记录订单日志...
========== 事务消息消费完成 ==========
```

**异常场景日志关键点：**
```
测试异常场景：创建金额为负数的订单...
开始执行本地事务...
订单金额不能为0或负数...
本地事务执行失败，回滚事务消息...
# 注意：消费者不会有任何日志输出，因为消息被回滚了
```

## 事务回查说明

当 `executeLocalTransaction()` 返回 `UNKNOWN` 状态，或者长时间未返回结果时，RocketMQ Broker 会定期回查事务状态：

- 默认回查间隔：60秒
- 最大回查次数：15次
- 超过最大回查次数后，消息会被删除

回查逻辑（`checkLocalTransaction()`）：
1. 从消息中提取订单ID
2. 查询数据库，检查订单是否存在
3. 如果订单存在，返回 COMMIT
4. 如果订单不存在，返回 ROLLBACK

## 注意事项

### 1. 幂等性
- 本地事务操作必须是幂等的，因为回查可能会多次执行
- 消费端也需要实现幂等性，防止重复消费

### 2. 事务状态
- `executeLocalTransaction()` 应该明确返回 COMMIT 或 ROLLBACK
- 只在无法确定本地事务状态时返回 UNKNOWN
- 避免一直返回 UNKNOWN，这会导致频繁回查

### 3. 性能考虑
- 事务消息的性能略低于普通消息
- 适用于对一致性要求高但并发量不是特别大的场景
- 如果并发量特别大，可以考虑异步化本地事务

### 4. 消息重试
- 消费失败时，RocketMQ 会自动重试
- 重试次数用尽后，消息会进入死信队列
- 建议对死信队列进行监控和处理

## 生产环境建议

1. **数据库事务**：使用真实的数据库事务，而不是内存 Map
2. **分布式锁**：对于幂等性要求高的场景，使用分布式锁
3. **监控告警**：监控消息发送失败率、消费失败率、死信队列
4. **日志记录**：完整记录事务消息的执行流程，便于问题排查
5. **超时设置**：合理设置本地事务超时时间，避免阻塞
6. **异常处理**：完善异常处理逻辑，避免未捕获的异常影响事务状态判断

## 与其他分布式事务方案对比

| 方案 | 优点 | 缺点 | 适用场景 |
|------|------|------|----------|
| RocketMQ事务消息 | 性能好、吞吐量高、最终一致性 | 需要业务实现回查逻辑 | 对性能要求高、可接受最终一致性 |
| Seata AT模式 | 无侵入、强一致性 | 性能开销大、复杂度高 | 强一致性要求、跨服务事务 |
| TCC | 灵活、可控性强 | 开发成本高、侵入性强 | 核心业务、对资金准确性要求高 |
| 本地消息表 | 简单、易理解 | 需要额外的表、定时任务 | 小型项目、业务简单 |

## 参考资料

- [RocketMQ 官方文档](https://rocketmq.apache.org/)
- [RocketMQ Spring Boot Starter](https://github.com/apache/rocketmq-spring)
- [分布式事务最佳实践](https://developer.aliyun.com/article/769627)
