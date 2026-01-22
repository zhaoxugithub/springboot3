# Spring Bean 生命周期演示案例 - 文件索引

## 📂 目录结构

```
com.atguigu.boot.lifecycle/
│
├── 📘 文档文件
│   ├── README.md                    # 完整说明文档（推荐先阅读）
│   ├── QUICK_START.md               # 快速开始指南
│   ├── INDEX.md                     # 本文件（文件索引）
│   └── BeanLifecycleFlow.java       # 生命周期流程图（文本版）
│
└── 📝 代码文件
    ├── LifecycleBean.java           # ⭐ 主要演示类（完整生命周期）
    ├── MyBeanPostProcessor.java     # ⭐ Bean 后置处理器
    ├── LifecycleConfig.java         # 配置类
    └── LifecycleTestRunner.java     # 测试运行器
```

## 🎯 阅读顺序建议

### 新手学习路线

1. **QUICK_START.md** - 快速了解如何运行案例
2. **LifecycleBean.java** - 查看完整的生命周期代码
3. **MyBeanPostProcessor.java** - 理解后置处理器的作用
4. **BeanLifecycleFlow.java** - 查看生命周期流程图
5. **README.md** - 深入学习理论知识和最佳实践

### 进阶学习路线

1. **README.md** - 先看理论（生命周期完整流程）
2. **BeanLifecycleFlow.java** - 看流程图加深理解
3. **LifecycleBean.java** - 代码对照理论学习
4. **MyBeanPostProcessor.java** - 学习 Spring 扩展点
5. **LifecycleConfig.java** - 学习配置方式
6. **LifecycleTestRunner.java** - 学习如何测试

## 📚 文件详细说明

### 1. LifecycleBean.java ⭐ 核心
**作用**: 演示 Bean 完整生命周期的所有阶段

**实现的接口**:
- `BeanNameAware` - 获取 Bean 名称
- `BeanFactoryAware` - 获取 BeanFactory
- `ApplicationContextAware` - 获取 ApplicationContext
- `InitializingBean` - 初始化回调
- `DisposableBean` - 销毁回调

**关键方法**:
- 构造器 - 第1步
- `setProperty()` - 第2步：属性注入
- `setBeanName()` - 第3步
- `setBeanFactory()` - 第4步
- `setApplicationContext()` - 第5步
- `postConstruct()` - 第7步：@PostConstruct
- `afterPropertiesSet()` - 第8步
- `customInit()` - 第9步：自定义初始化
- `doSomething()` - 业务方法
- `preDestroy()` - 第11步：@PreDestroy
- `destroy()` - 第12步
- `customDestroy()` - 第13步：自定义销毁

**学习重点**:
- 每个阶段的执行顺序
- 每个接口的作用
- 初始化和销毁的三种方式

---

### 2. MyBeanPostProcessor.java ⭐ 扩展点
**作用**: Spring 框架最重要的扩展点之一

**实现的接口**:
- `BeanPostProcessor` - Bean 后置处理器

**关键方法**:
- `postProcessBeforeInitialization()` - 第6步：初始化前处理
- `postProcessAfterInitialization()` - 第10步：初始化后处理（AOP 实现原理）

**学习重点**:
- BeanPostProcessor 的执行时机
- 如何在此创建代理对象
- Spring AOP 的实现原理

---

### 3. LifecycleConfig.java
**作用**: Bean 配置类，演示如何配置 initMethod 和 destroyMethod

**关键注解**:
- `@Configuration` - 配置类
- `@Bean` - 注册 Bean
  - `initMethod` - 指定初始化方法
  - `destroyMethod` - 指定销毁方法

**学习重点**:
- 如何通过配置类注册 Bean
- initMethod 和 destroyMethod 的使用
- 这种方式的优点（解耦）

---

### 4. LifecycleTestRunner.java
**作用**: 测试运行器，在应用启动后自动执行

**实现的接口**:
- `CommandLineRunner` - Spring Boot 提供的启动后执行接口

**学习重点**:
- 如何在应用启动后执行代码
- 构造器注入的使用
- 如何获取和使用 ApplicationContext

---

### 5. BeanLifecycleFlow.java
**作用**: 生命周期流程图和说明（纯文档类）

**内容**:
- ASCII 流程图
- 各阶段详细说明
- 核心扩展点介绍
- 最佳实践建议
- 常见应用场景

**学习重点**:
- 从宏观角度理解整个生命周期
- 记住关键步骤和顺序

---

### 6. README.md
**作用**: 完整的学习文档

**包含内容**:
- 项目说明和学习目标
- 完整的生命周期流程
- 核心知识点详解
- 实际应用场景
- 面试要点
- 最佳实践

**学习重点**:
- 系统化学习 Bean 生命周期
- 理解各个扩展点的应用场景
- 掌握面试常考知识点

---

### 7. QUICK_START.md
**作用**: 快速开始指南

**包含内容**:
- 如何运行应用
- 预期的输出结果
- 文件清单
- 学习重点提示

**学习重点**:
- 快速上手运行案例
- 观察实际的生命周期执行过程

---

## 🔍 核心概念速查

| 概念 | 文件位置 | 说明 |
|------|---------|------|
| Bean 生命周期完整流程 | README.md | 13 个步骤详解 |
| BeanPostProcessor | MyBeanPostProcessor.java | Spring 扩展点，AOP 原理 |
| Aware 接口 | LifecycleBean.java | 获取容器信息 |
| 初始化方法 | LifecycleBean.java | 三种方式对比 |
| 销毁方法 | LifecycleBean.java | 三种方式对比 |
| 配置方式 | LifecycleConfig.java | @Bean 注解使用 |

## 🎓 学习建议

### 理论学习
1. 先看 README.md 了解理论
2. 对照 BeanLifecycleFlow.java 理解流程
3. 研究 LifecycleBean.java 看具体实现

### 实践学习
1. 运行应用观察输出
2. 修改代码做实验（如注释掉某些接口）
3. 尝试添加自己的业务逻辑

### 深入学习
1. 研究 MyBeanPostProcessor 的原理
2. 查看 Spring 源码中 BeanPostProcessor 的实现
3. 了解 Spring AOP 如何基于此实现

## 💡 实验建议

### 实验1：观察完整流程
- 运行应用，观察所有日志输出
- 关闭应用，观察销毁过程

### 实验2：测试不同初始化方式
- 分别注释掉 @PostConstruct、InitializingBean、init-method
- 观察执行顺序的变化

### 实验3：测试 BeanPostProcessor
- 在 postProcessAfterInitialization 中返回代理对象
- 观察对 Bean 行为的影响

### 实验4：Aware 接口的使用
- 通过 ApplicationContext 获取其他 Bean
- 尝试动态创建 Bean

## 📞 学习资源

- **Spring 官方文档**: [Bean 生命周期](https://docs.spring.io/spring-framework/reference/core/beans/factory-nature.html)
- **源码位置**: `AbstractAutowireCapableBeanFactory.doCreateBean()`
- **相关接口**: `BeanPostProcessor`, `InitializingBean`, `DisposableBean`

---

**提示**: 建议按照推荐的阅读顺序学习，并动手运行和修改代码来加深理解。

