# Spring Bean 生命周期案例 - 快速开始

## 🚀 快速运行

### 1. 进入项目目录
```bash
cd /Users/serendipity/IdeaProjects/springboot3/boot3-01-demo
```

### 2. 运行应用
```bash
mvn spring-boot:run
```

或在 IDE 中运行 `MainApplication` 类

### 3. 观察控制台输出

你将看到完整的 Bean 生命周期日志：

```
1. 【构造器】LifecycleBean 构造方法执行
2. 【属性注入】setProperty 方法执行，property = 测试属性值
3. 【BeanNameAware】setBeanName 方法执行，beanName = lifecycleBean
4. 【BeanFactoryAware】setBeanFactory 方法执行
5. 【ApplicationContextAware】setApplicationContext 方法执行
6. 【BeanPostProcessor】postProcessBeforeInitialization 执行，beanName = lifecycleBean
7. 【@PostConstruct】postConstruct 方法执行
8. 【InitializingBean】afterPropertiesSet 方法执行
9. 【init-method】customInit 自定义初始化方法执行
10. 【BeanPostProcessor】postProcessAfterInitialization 执行，beanName = lifecycleBean
```

### 4. 关闭应用观察销毁过程

按 `Ctrl+C` 关闭应用，你将看到：

```
11. 【@PreDestroy】preDestroy 方法执行
12. 【DisposableBean】destroy 方法执行
13. 【destroy-method】customDestroy 自定义销毁方法执行
```

## 📁 案例文件清单

```
lifecycle/
├── LifecycleBean.java              # 主要演示类（含详细注释）
├── MyBeanPostProcessor.java        # Bean 后置处理器
├── LifecycleConfig.java            # 配置类
├── LifecycleTestRunner.java        # 测试运行器
├── BeanLifecycleFlow.java          # 生命周期流程图
├── README.md                       # 详细说明文档
└── QUICK_START.md                  # 本文件
```

## 🎯 学习重点

1. **创建阶段**: 构造器 → 属性注入 → Aware 接口
2. **初始化阶段**: BeanPostProcessor → 初始化方法
3. **销毁阶段**: 销毁方法
4. **核心扩展点**: BeanPostProcessor（AOP 实现原理）

## 💡 代码亮点

- ✅ 完整的生命周期演示
- ✅ 详细的中文注释
- ✅ 清晰的日志输出
- ✅ 实际应用场景说明
- ✅ 最佳实践建议

## 📚 更多信息

查看 `README.md` 获取完整的文档和深入讲解。

