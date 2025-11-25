# boot3-02-demo 项目详解

## 项目概述

`boot3-02-demo` 是一个 Spring Boot 3 演示项目，主要用于展示 **Spring Boot 配置属性绑定** 和 **条件注解** 的使用方式。该项目演示了如何将配置文件中的属性值自动注入到 Java Bean 中，以及如何根据不同条件动态注册组件。

---

## 配置文件解析

### application.properties 文件内容

```properties
pig.id=1
pig.name=pig
pig.age=5
sheep.id=1
sheep.name=sleep
sheep.age=5
```

这个配置文件定义了两组属性配置：

1. **pig 前缀的属性**：包含 `id`、`name`、`age` 三个字段，用于配置 Pig 对象的属性值
2. **sheep 前缀的属性**：同样包含 `id`、`name`、`age` 三个字段，用于配置 Sheep 对象的属性值

---

## 核心知识点

### 1. 属性绑定的两种方式

项目中演示了两种将配置文件属性绑定到对象的方式：

#### 方式一：@Bean + @ConfigurationProperties（Pig 类）

**Pig.java** - 纯 POJO 类，不包含任何 Spring 注解：

```java
@Data
public class Pig {
    private Long id;
    private String name;
    private Integer age;
}
```

**AppConfig.java** - 通过配置类的 @Bean 方法进行属性绑定：

```java
@Bean
@ConfigurationProperties(prefix = "pig")
public Pig pig() {
    return new Pig(); // 手动创建 Pig 实例
}
```

**工作原理**：
- Spring Boot 会创建 Pig 实例并将其注册为 Bean
- `@ConfigurationProperties(prefix = "pig")` 告诉 Spring 将配置文件中 `pig.*` 开头的属性绑定到这个对象
- 配置文件中的 `pig.id=1` 会自动注入到 Pig 对象的 `id` 字段
- 配置文件中的 `pig.name=pig` 会自动注入到 `name` 字段
- 配置文件中的 `pig.age=5` 会自动注入到 `age` 字段

**优点**：
- Pig 类保持纯粹的 POJO，不依赖 Spring 框架
- 可以在 `@Bean` 方法中添加额外的初始化逻辑
- 灵活性高，适合需要精细控制 Bean 创建过程的场景

---

#### 方式二：@EnableConfigurationProperties（Sheep 类）

**Sheep.java** - 带有 @ConfigurationProperties 注解的类：

```java
@Data
@ConfigurationProperties(prefix = "sheep")
public class Sheep {
    private Long id;
    private String name;
    private Integer age;
}
```

**AppConfig.java** - 使用 @EnableConfigurationProperties 启用属性绑定：

```java
@EnableConfigurationProperties(Sheep.class)
@SpringBootConfiguration
public class AppConfig {
    // 配置类内容
}
```

**工作原理**：
- `@EnableConfigurationProperties(Sheep.class)` 做了两件事：
    1. 将 Sheep 类注册为 Spring Bean
    2. 启用 Sheep 类的属性绑定功能
- Spring Boot 自动将 `sheep.*` 开头的属性注入到 Sheep 对象中

**适用场景**：
注释中明确说明了使用场景：

```java
// SpringBoot默认只扫描自己主程序所在的包。
// 如果导入第三方包，即使组件上标注了 @Component、@ConfigurationProperties 注解，也没用。
// 因为组件都扫描不进来
```

这种方式特别适合：
- 引入第三方库中的配置类（无法修改源码添加 @Component）
- 配置类不在组件扫描范围内
- 需要明确声明启用哪些配置类

---

### 2. 两种方式的对比

| 特性 | @Bean + @ConfigurationProperties | @EnableConfigurationProperties |
|-----|----------------------------------|--------------------------------|
| **配置类注解** | 不需要 | 需要 @ConfigurationProperties |
| **Bean 创建** | 手动在 @Bean 方法中创建 | 自动创建 |
| **灵活性** | 高，可添加初始化逻辑 | 标准，自动处理 |
| **适用场景** | 需要自定义创建逻辑 | 第三方类或简单绑定 |
| **代码位置** | 配置类的方法中 | 配置类头部声明 |

---

### 3. 条件注解演示（AppConfig2）

项目中还包含了 **条件注解** 的使用示例：

```java
@ConditionalOnMissingClass(value = "com.alibaba.druid.FastsqlException")
@SpringBootConfiguration
public class AppConfig2 {
    
    @ConditionalOnClass(name = "com.alibaba.druidFa.stsqlException")
    @Bean
    public Cat cat01() {
        return new Cat();
    }
    
    @Bean
    public Dog dog01() {
        return new Dog();
    }
    
    @ConditionalOnBean(value = Dog.class)
    @Bean
    public User zhangsan() {
        return new User();
    }
    
    @ConditionalOnMissingBean(value = Dog.class)
    @Bean
    public User lisi() {
        return new User();
    }
}
```

**条件注解说明**：

- **@ConditionalOnMissingClass**：类级别，当指定的类不存在时，整个配置类才生效
- **@ConditionalOnClass**：方法级别，当指定的类存在时，该 Bean 才会被创建
- **@ConditionalOnBean**：当容器中存在指定的 Bean 时才创建（zhangsan 只有在 Dog 存在时才创建）
- **@ConditionalOnMissingBean**：当容器中不存在指定的 Bean 时才创建（lisi 只有在 Dog 不存在时才创建）

这些条件注解实现了 **互斥逻辑**：zhangsan 和 lisi 两个 User Bean 只会有一个被创建，取决于 Dog Bean 是否存在。

---

### 4. 主启动类验证

**Boot302DemoApplication.java** 中验证了配置绑定的效果：

```java
public static void main(String[] args) {
    var ioc = SpringApplication.run(Boot302DemoApplication.class, args);
    
    Pig pig = ioc.getBean(Pig.class);
    System.out.println("pig: " + pig);  // 输出: pig: Pig{id=1, name='pig', age=5}
    
    Sheep sheep = ioc.getBean(Sheep.class);
    System.out.println("sheep: " + sheep);  // 输出: sheep: Sheep{id=1, name='sleep', age=5}
}
```

从容器中获取 Pig 和 Sheep 对象后，会看到它们的属性已经被正确注入：
- Pig 对象的属性值来自 `pig.*` 配置
- Sheep 对象的属性值来自 `sheep.*` 配置

---

## 项目依赖

从 `pom.xml` 可以看到项目使用了：

- **Spring Boot 3.4.0**：最新版本的 Spring Boot
- **Java 21**：使用现代 Java 版本
- **Lombok 1.18.30**：简化 getter/setter 代码
- **Thymeleaf**：模板引擎（虽然本示例未使用）
- **Druid 1.2.16**：阿里巴巴的数据库连接池（用于条件注解演示）

---

## 学习要点总结

### 配置文件中的属性定义

```properties
pig.id=1
pig.name=pig
pig.age=5
sheep.id=1
sheep.name=sleep
sheep.age=5
```

这些属性通过 **属性前缀** 进行分组：
- `pig.*` 前缀的属性会绑定到 Pig 对象
- `sheep.*` 前缀的属性会绑定到 Sheep 对象

### 属性绑定特性

1. **自动类型转换**：配置文件中的字符串会自动转换为对应的类型（String → Long、Integer 等）
2. **松散绑定**：支持多种命名格式（如 `pig.user-name`、`pig.user_name`、`pig.userName` 都能绑定到 `userName` 字段）
3. **默认值支持**：如果配置文件中没有对应属性，字段保持默认值
4. **嵌套属性**：支持复杂对象的嵌套属性绑定

### 最佳实践建议

1. **第三方配置类**：使用 `@EnableConfigurationProperties` 导入
2. **自定义初始化**：使用 `@Bean + @ConfigurationProperties` 方式
3. **简单绑定**：优先使用 `@EnableConfigurationProperties`，代码更简洁
4. **条件装配**：使用条件注解实现按需加载，提高应用灵活性

---

## 执行结果预期

运行主程序后，控制台会输出：

```
pig: Pig{id=1, name='pig', age=5}
sheep: Sheep{id=1, name='sleep', age=5}
```

这证明了配置文件中的属性已经成功绑定到对应的 Java 对象中，Spring Boot 的自动配置机制正常工作。