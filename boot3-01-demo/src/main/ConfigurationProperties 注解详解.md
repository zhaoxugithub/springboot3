# @ConfigurationProperties 注解详解

## 核心结论

**`@ConfigurationProperties` 注解本身不能将对象添加到 Spring 容器中！**

- **作用**：`@ConfigurationProperties` 只负责将配置文件中的属性值绑定到对象的字段上
- **需要配合使用**：必须配合其他注解才能将对象注册为 Spring Bean

---

## 三种将配置属性类注册到容器的方法

### 方法1：@Component + @ConfigurationProperties（推荐，简单直接）

```java
@Component  // 将 Teacher 注册为 Spring Bean
@Data
@ConfigurationProperties(prefix = "tea")
public class Teacher {
    private String name;
    private String age;
    private String type;
}
```

**优点**：
- 简单直接，在类上加一个注解即可
- 适合在自己项目内部使用的配置类

**适用场景**：配置类在组件扫描范围内

---

### 方法2：@EnableConfigurationProperties（推荐，用于第三方类）

在配置类或主启动类上使用：

```java
@SpringBootApplication
@EnableConfigurationProperties(Teacher.class)  // 注册 Teacher 为 Bean
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
```

Teacher 类只需要 `@ConfigurationProperties`：

```java
@Data
@ConfigurationProperties(prefix = "tea")
public class Teacher {
    private String name;
    private String age;
    private String type;
}
```

**优点**：
- 配置类无需添加 `@Component`，更加解耦
- 适合引入第三方配置类（无法修改源码时）
- 明确声明哪些配置类需要被启用

**适用场景**：
- 引入第三方库的配置类
- 配置类不在组件扫描范围内
- 想要明确控制哪些配置类生效

---

### 方法3：@Bean + @ConfigurationProperties（推荐，灵活性最高）

在配置类中使用 `@Bean` 方法：

```java
@Configuration
public class MyConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "tea")
    public Teacher teacher() {
        return new Teacher();
    }
}
```

Teacher 类不需要任何 Spring 注解：

```java
@Data
public class Teacher {
    private String name;
    private String age;
    private String type;
}
```

**优点**：
- 最灵活，可以在创建 Bean 时添加额外逻辑
- 配置类完全是 POJO，不依赖 Spring
- 可以根据条件创建不同的实例

**适用场景**：
- 需要在创建 Bean 时执行额外逻辑
- 同一个类需要创建多个不同配置的实例
- 保持配置类的纯粹性（不依赖框架）

---

## 配置文件示例

```yaml
# application.yml
tea:
  name: 张三
  age: 30
  type: 语文老师
```

或

```properties
# application.properties
tea.name=张三
tea.age=30
tea.type=语文老师
```

---

## 三种方法对比总结

| 方法 | 注解位置 | 优点 | 适用场景 |
|-----|---------|------|---------|
| **@Component** | 在配置类上 | 简单直接 | 项目内部配置类 |
| **@EnableConfigurationProperties** | 在启动类/配置类上 | 解耦，适合第三方类 | 第三方配置类，或不在扫描范围 |
| **@Bean** | 在配置方法上 | 最灵活，可添加逻辑 | 需要额外初始化逻辑 |

---

## 注意事项

1. **必须配合使用**：单独使用 `@ConfigurationProperties` 不会将类注册到容器
2. **属性绑定**：Spring Boot 会自动将配置文件中的值注入到对应字段
3. **松散绑定**：支持 `tea.userName`、`tea.user-name`、`tea.user_name` 等多种写法
4. **类型转换**：Spring 会自动进行类型转换（String → int、boolean 等）
5. **JSR-303 校验**：配合 `@Validated` 可以对配置进行校验

---

## 你当前的实现（方法1）

你的 Teacher 类现在使用的是 **方法1**，已经正确配置：

```java
@Component  // ✅ 将 Teacher 注册为 Spring Bean
@Data
@ConfigurationProperties(prefix = "tea")  // ✅ 绑定配置文件中的属性
public class Teacher {
    private String name;
    private String age;
    private String type;
}
```

这样 Teacher 类就已经成功注册到 Spring 容器中了！