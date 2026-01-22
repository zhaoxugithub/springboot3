# boot3-07-core 项目详解

## 项目概述

`boot3-07-core` 是一个 **Spring Boot 3 核心特性演示项目**，主要展示 Spring Boot 的核心机制和高级特性。这是整个 springboot3 系列学习项目中的第 7 个模块，专注于演示 Spring Boot 的核心功能。

---

## 项目基本信息

### 技术栈
- **Spring Boot**: 3.4.0（最新版本）
- **Java**: 21（最新 LTS 版本）
- **构建工具**: Maven
- **开发辅助**: Lombok 1.18.30
- **配置处理**: Spring Boot Configuration Processor

### Maven 坐标
```xml
<groupId>com.atguigu</groupId>
<artifactId>boot3-07-core</artifactId>
<version>0.0.1-SNAPSHOT</version>
```

---

## 项目核心技术点

### 1. **配置属性绑定 (@ConfigurationProperties)**

#### 应用场景
将配置文件中的属性自动绑定到 Java Bean，实现类型安全的配置管理。

#### 示例：RobotProperties.java
```java
@ConfigurationProperties(prefix = "robot")  // 绑定 robot.* 开头的配置
@Component
@Data
public class RobotProperties {
    private String name;
    private String age;
    private String email;
}
```

#### 配置文件：application.properties
```properties
server.port=8888
robot.name=王五
robot.age=19
robot.email=haha@qq.com
```

#### 技术优势
- **类型安全**：编译时检查，避免运行时类型转换错误
- **IDE 支持**：通过 spring-boot-configuration-processor 提供属性提示
- **集中管理**：配置和代码分离，易于维护
- **验证支持**：可以配合 JSR-303 进行参数校验

---

### 2. **事件驱动编程 (Event-Driven Architecture)**

Spring Boot 提供了完整的事件发布-订阅机制，实现组件间的解耦通信。

#### 核心组件

##### a) 自定义事件：LoginSuccessEvent.java
```java
public class LoginSuccessEvent extends ApplicationEvent {
    /**
     * @param source 代表是谁登录成功了
     */
    public LoginSuccessEvent(UserEntity source) {
        super(source);
    }
}
```

**设计说明**：
- 继承 `ApplicationEvent` 是推荐做法
- 事件对象包含事件相关的数据（这里是登录用户信息）
- 事件是不可变的，保证线程安全

##### b) 事件发布器：EventPublisher.java
```java
@Service
public class EventPublisher {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    
    public void publishLoginSuccessEvent(UserEntity user) {
        // 发布事件
        applicationEventPublisher.publishEvent(new LoginSuccessEvent(user));
    }
}
```

##### c) 事件监听器（方式一）：使用 @EventListener 注解
```java
@Service
public class AccountService {
    @EventListener
    public void onLoginSuccess(LoginSuccessEvent event) {
        UserEntity user = (UserEntity) event.getSource();
        System.out.println("AccountService 监听到用户登录：" + user.getUsername());
        // 执行账户相关的业务逻辑
    }
}
```

##### d) 事件监听器（方式二）：实现 ApplicationListener 接口
```java
@Service
public class CouponService implements ApplicationListener<LoginSuccessEvent> {
    @Override
    public void onApplicationEvent(LoginSuccessEvent event) {
        UserEntity user = (UserEntity) event.getSource();
        System.out.println("CouponService 监听到用户登录：" + user.getUsername());
        // 发放优惠券
    }
}
```

#### 事件驱动的优势
- **解耦**：发布者和订阅者互不依赖
- **可扩展**：新增监听器无需修改现有代码
- **异步支持**：可配合 @Async 实现异步处理
- **事务传播**：支持事务事件（@TransactionalEventListener）

#### 实际应用场景
1. **用户登录成功**
   - 记录登录日志
   - 发送登录通知
   - 更新用户积分
   - 发放登录优惠券

2. **订单创建**
   - 扣减库存
   - 发送通知
   - 记录操作日志
   - 触发物流系统

---

### 3. **应用生命周期监听 (SpringApplicationRunListener)**

Spring Boot 提供了多个阶段的生命周期钩子，可以在应用启动的不同阶段执行自定义逻辑。

#### 生命周期监听器：MyAppListener.java
```java
public class MyAppListener implements SpringApplicationRunListener {
    
    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        System.out.println("=====starting=====正在启动======");
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, 
                                   ConfigurableEnvironment environment) {
        System.out.println("=====environmentPrepared=====环境准备完成======");
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        System.out.println("=====contextPrepared=====ioc容器准备完成======");
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        System.out.println("=====contextLoaded=====ioc容器加载完成======");
    }

    @Override
    public void started(ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("=====started=====启动完成======");
    }

    @Override
    public void ready(ConfigurableApplicationContext context, Duration timeTaken) {
        System.out.println("=====ready=====准备就绪======");
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        System.out.println("=====failed=====应用启动失败======");
    }
}
```

#### Spring Boot 启动流程详解

```
1. 【引导阶段】
   ├── starting()              应用开始启动
   │   └── BootstrapContext 创建
   │
   ├── environmentPrepared()   环境准备完成
   │   └── 配置文件加载、环境变量绑定

2. 【启动阶段】
   ├── contextPrepared()       IoC 容器创建并准备好
   │   └── 容器已创建，但主配置类未加载
   │
   ├── contextLoaded()         IoC 容器加载完成
   │   └── 主配置类已加载，但 Bean 未创建
   │
   ├── started()               IoC 容器刷新完成
   │   └── 所有 Bean 已创建，但 Runner 未调用
   │
   └── ready()                 应用准备就绪
       └── 所有 Runner 已执行完成

3. 【运行阶段】
   └── 应用正常运行，可以接收请求

4. 【失败处理】
   └── failed()               任何阶段失败时调用
```

#### 注册方式
需要在 `META-INF/spring.factories` 中注册：
```properties
org.springframework.boot.SpringApplicationRunListener=\
com.atguigu.boot3.core.listener.MyAppListener
```

#### 应用场景
- **starting**: 初始化日志系统、加载启动配置
- **environmentPrepared**: 根据环境变量进行预配置
- **contextPrepared**: 注册自定义 Bean 定义
- **contextLoaded**: 准备应用上下文
- **started**: 启动后台任务、预热缓存
- **ready**: 应用就绪通知、健康检查
- **failed**: 启动失败告警、清理资源

---

### 4. **应用启动后执行 (ApplicationRunner & CommandLineRunner)**

#### 主启动类：Boot307CoreApplication.java
```java
@SpringBootApplication
public class Boot307CoreApplication {
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Boot307CoreApplication.class);
        application.run(args);
    }

    @Bean
    public ApplicationRunner applicationRunner() {
        return args -> {
            System.out.println("===ApplicationRunner 运行了.....");
            // 执行应用启动后的初始化逻辑
        };
    }

    @Bean
    public CommandLineRunner commandLineRunner() {
        return args -> {
            System.out.println("===CommandLineRunner 运行了.....");
            // 执行命令行参数相关的逻辑
        };
    }
}
```

#### 两种 Runner 的区别

| 特性 | ApplicationRunner | CommandLineRunner |
|-----|------------------|-------------------|
| **参数类型** | ApplicationArguments（结构化） | String[]（原始数组） |
| **参数解析** | 已解析的选项和参数 | 原始命令行参数 |
| **使用便利性** | 更方便（支持选项名访问） | 较原始 |
| **典型用途** | 业务初始化、数据预热 | 简单的命令行工具 |

#### 执行顺序
如果有多个 Runner，可以使用 `@Order` 注解控制执行顺序：
```java
@Bean
@Order(1)
public ApplicationRunner firstRunner() {
    return args -> System.out.println("第一个执行");
}

@Bean
@Order(2)
public ApplicationRunner secondRunner() {
    return args -> System.out.println("第二个执行");
}
```

#### 应用场景
- **数据初始化**：加载初始数据、预热缓存
- **定时任务启动**：启动后台定时任务
- **系统检查**：检查数据库连接、第三方服务状态
- **配置验证**：验证关键配置是否正确
- **数据迁移**：执行数据迁移脚本

---

### 5. **可选注解功能（已注释）**

项目中展示了一些可选的高级特性（默认关闭）：

#### a) @EnableWebMvc
```java
// @EnableWebMvc  // 全面接管 Spring MVC，禁用所有 MVC 底层的自动配置
```

**作用**：
- 完全自定义 Spring MVC 配置
- 禁用 Spring Boot 的 MVC 自动配置
- 需要手动配置所有 MVC 组件

**使用场景**：
- 需要完全自定义 MVC 行为
- 与遗留系统集成
- 特殊的 MVC 配置需求

⚠️ **注意**：一般不建议使用，会失去 Spring Boot 的自动配置优势。

#### b) @EnableAsync
```java
// @EnableAsync  // 开启异步任务支持
```

**作用**：启用 Spring 的异步方法执行能力

**使用方式**：
```java
@Service
public class AsyncService {
    @Async
    public CompletableFuture<String> asyncMethod() {
        // 在独立线程中执行
        return CompletableFuture.completedFuture("结果");
    }
}
```

**应用场景**：
- 发送邮件/短信通知（不阻塞主流程）
- 日志记录（异步写入）
- 数据统计（后台计算）
- 文件处理（大文件异步处理）

#### c) @EnableScheduling
```java
// @EnableScheduling  // 开启定时任务支持
```

**作用**：启用 Spring 的定时任务功能

**使用方式**：
```java
@Component
public class ScheduledTasks {
    @Scheduled(fixedRate = 5000)  // 每5秒执行一次
    public void reportCurrentTime() {
        System.out.println("当前时间：" + new Date());
    }
    
    @Scheduled(cron = "0 0 2 * * ?")  // 每天凌晨2点执行
    public void performDailyTask() {
        // 执行日常任务
    }
}
```

**应用场景**：
- 数据同步（定期同步数据）
- 缓存刷新（定期更新缓存）
- 报表生成（每日/每周报表）
- 数据清理（清理过期数据）

---

## 项目结构分析

```
boot3-07-core/
├── src/main/java/com/atguigu/boot3/core/
│   ├── Boot307CoreApplication.java          # 主启动类
│   ├── controller/
│   │   ├── LoginController.java             # 登录控制器（触发事件）
│   │   └── robot/
│   │       └── RobotController.java         # 配置属性演示
│   ├── entity/
│   │   └── UserEntity.java                  # 用户实体
│   ├── event/
│   │   ├── EventPublisher.java              # 事件发布器
│   │   └── LoginSuccessEvent.java           # 登录成功事件
│   ├── listener/
│   │   ├── MyListener.java                  # 事件监听器
│   │   └── MyAppListener.java               # 应用生命周期监听
│   ├── service/
│   │   ├── AccountService.java              # 账户服务（事件监听）
│   │   ├── CouponService.java               # 优惠券服务（事件监听）
│   │   ├── SysService.java                  # 系统服务（事件监听）
│   │   └── HahaService.java                 # 其他服务
│   └── robot/
│       ├── properties/
│       │   └── RobotProperties.java         # Robot 配置属性类
│       ├── service/
│       │   └── RobotService.java            # Robot 业务服务
│       └── controller/
│           └── RobotController.java         # Robot 控制器
└── src/main/resources/
    └── application.properties                # 应用配置文件
```

---

## 核心技术总结

### 1. Spring Boot 配置管理
- **@ConfigurationProperties**: 类型安全的配置绑定
- **spring-boot-configuration-processor**: IDE 配置提示支持
- **外部化配置**: 配置文件、环境变量、命令行参数

### 2. Spring 事件机制
- **ApplicationEvent**: 自定义事件基类
- **ApplicationEventPublisher**: 事件发布接口
- **@EventListener**: 声明式事件监听
- **ApplicationListener**: 接口式事件监听
- **事件驱动架构**: 实现组件解耦

### 3. Spring Boot 生命周期
- **SpringApplicationRunListener**: 应用生命周期监听
- **ApplicationRunner**: 应用启动后执行（推荐）
- **CommandLineRunner**: 命令行应用启动后执行
- **@PostConstruct**: Bean 初始化后执行
- **InitializingBean**: Bean 属性设置后执行

### 4. Spring Boot 自动配置
- **@SpringBootApplication**: 核心组合注解
- **@EnableAutoConfiguration**: 启用自动配置
- **@ComponentScan**: 组件扫描
- **条件注解**: @ConditionalOnXxx 系列

### 5. Spring 增强特性
- **@Async**: 异步方法执行
- **@Scheduled**: 定时任务
- **@EnableWebMvc**: 完全自定义 MVC 配置

---

## 与其他模块的关系

### 相关模块对比

| 模块 | 主要内容 | 与 boot3-07-core 的关系 |
|-----|---------|----------------------|
| **boot3-01-demo** | 基础入门、快速开始 | 基础 |
| **boot3-02-demo** | 配置属性绑定、条件注解 | boot3-07-core 深入配置管理 |
| **boot3-03-logging** | 日志配置 | 补充：应用监控 |
| **boot3-04-web** | Spring MVC、Web 开发 | boot3-07-core 是核心机制 |
| **boot3-05-ssm** | MyBatis 整合 | 数据访问层 |
| **boot3-06-features** | Spring Boot 特性 | boot3-07-core 是核心特性 |
| **boot3-08-robot-starter** | 自定义 Starter | 扩展 boot3-07-core 的配置 |

---

## 实战应用场景

### 场景1：用户登录系统
```java
@RestController
@RequestMapping("/login")
public class LoginController {
    
    @Autowired
    private EventPublisher eventPublisher;
    
    @PostMapping
    public Result login(@RequestBody LoginRequest request) {
        // 1. 验证用户名密码
        UserEntity user = validateUser(request);
        
        // 2. 发布登录成功事件
        eventPublisher.publishLoginSuccessEvent(user);
        
        // 3. 返回登录结果
        return Result.success(user);
    }
}

// 各个服务监听事件并执行相应逻辑
@Service
public class AccountService {
    @EventListener
    public void onLoginSuccess(LoginSuccessEvent event) {
        // 更新最后登录时间
    }
}

@Service
public class CouponService {
    @EventListener
    public void onLoginSuccess(LoginSuccessEvent event) {
        // 发放登录优惠券
    }
}

@Service
public class LogService {
    @EventListener
    @Async  // 异步执行，不阻塞主流程
    public void onLoginSuccess(LoginSuccessEvent event) {
        // 记录登录日志
    }
}
```

### 场景2：应用启动初始化
```java
@Component
public class DataInitializer implements ApplicationRunner {
    
    @Autowired
    private RedisTemplate redisTemplate;
    
    @Autowired
    private ConfigService configService;
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 1. 预热缓存
        warmUpCache();
        
        // 2. 加载系统配置
        loadSystemConfig();
        
        // 3. 检查数据库连接
        checkDatabaseConnection();
        
        System.out.println("应用初始化完成");
    }
}
```

### 场景3：配置管理
```java
@ConfigurationProperties(prefix = "app")
@Component
@Validated  // 支持 JSR-303 验证
@Data
public class AppProperties {
    
    @NotBlank(message = "应用名称不能为空")
    private String name;
    
    @Min(value = 1024, message = "端口号必须大于1024")
    private Integer port;
    
    @Email(message = "邮箱格式不正确")
    private String adminEmail;
    
    private Security security = new Security();
    
    @Data
    public static class Security {
        private Boolean enabled = true;
        private List<String> allowedOrigins = new ArrayList<>();
    }
}
```

---

## 学习建议

### 初学者路径
1. **理解基础概念**
   - Spring Boot 自动配置原理
   - IoC 容器和依赖注入
   - Spring Boot 启动流程

2. **掌握核心特性**
   - 配置属性绑定（@ConfigurationProperties）
   - 事件驱动编程（ApplicationEvent）
   - 生命周期管理（Runner、Listener）

3. **实践应用**
   - 搭建实际项目
   - 实现事件驱动的业务逻辑
   - 使用配置管理优化代码

### 进阶学习
1. **深入源码**
   - SpringApplication 启动流程
   - 自动配置原理（@Conditional）
   - 事件发布订阅机制

2. **性能优化**
   - 异步事件处理（@Async）
   - 懒加载配置
   - 启动时间优化

3. **架构设计**
   - 事件驱动架构（EDA）
   - CQRS 模式
   - 微服务事件总线

---

## 常见问题 FAQ

### Q1: @ConfigurationProperties 和 @Value 有什么区别？

**A:**
| 特性 | @ConfigurationProperties | @Value |
|-----|-------------------------|--------|
| 绑定方式 | 批量绑定到对象 | 单个属性注入 |
| 类型安全 | 支持，编译时检查 | 不支持，运行时转换 |
| 复杂对象 | 支持（嵌套对象、集合） | 不支持 |
| IDE提示 | 支持（需配置处理器） | 基本支持 |
| 松散绑定 | 支持（驼峰、下划线等） | 不支持 |
| 验证 | 支持 JSR-303 | 不支持 |
| **推荐场景** | 多个相关配置 | 单个简单配置 |

### Q2: 事件监听是同步还是异步的？

**A:** 
- 默认是**同步**的，在发布事件的线程中依次执行所有监听器
- 可以通过 `@Async` 注解实现异步监听：
```java
@Async
@EventListener
public void onLoginSuccess(LoginSuccessEvent event) {
    // 在独立线程中执行
}
```
- 需要在启动类上添加 `@EnableAsync` 启用异步支持

### Q3: ApplicationRunner 和 CommandLineRunner 执行顺序如何控制？

**A:**
使用 `@Order` 注解或实现 `Ordered` 接口：
```java
@Bean
@Order(1)  // 数字越小，优先级越高
public ApplicationRunner firstRunner() {
    return args -> System.out.println("first");
}

@Bean
@Order(2)
public ApplicationRunner secondRunner() {
    return args -> System.out.println("second");
}
```

### Q4: SpringApplicationRunListener 如何注册？

**A:**
必须通过 SPI 机制注册，在 `META-INF/spring.factories` 中配置：
```properties
org.springframework.boot.SpringApplicationRunListener=\
com.atguigu.boot3.core.listener.MyAppListener
```
不能使用 `@Component` 注解注册。

### Q5: 如何实现事务事件监听？

**A:**
使用 `@TransactionalEventListener` 注解：
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void handleAfterCommit(OrderCreatedEvent event) {
    // 事务提交后执行
}

@TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
public void handleAfterRollback(OrderCreatedEvent event) {
    // 事务回滚后执行
}
```

---

## 总结

**boot3-07-core** 项目是 Spring Boot 3 系列中的核心模块，深入展示了：

✅ **配置管理**：类型安全的配置绑定  
✅ **事件驱动**：解耦的组件通信机制  
✅ **生命周期**：应用启动的各个阶段  
✅ **启动初始化**：应用启动后的自定义逻辑  
✅ **高级特性**：异步、定时任务等扩展功能  

这些核心特性是构建企业级 Spring Boot 应用的基础，掌握它们将大大提升开发效率和代码质量。

---

## 参考资料

- [Spring Boot 官方文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Framework 事件机制](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events)
- [Spring Boot 配置属性](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)

