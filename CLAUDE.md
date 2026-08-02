# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build/Run

```bash
# Build everything
mvn -f pom.xml clean compile

# Build & run a specific module (every module is independently runnable)
mvn -f boot3-01-demo/pom.xml spring-boot:run

# Run a single test class
mvn -f boot3-01-demo/pom.xml test -Dtest=ClassName

# Run a specific test method
mvn -f boot3-01-demo/pom.xml test -Dtest=ClassName#methodName
```

All modules target **Java 21**, source encoding **UTF-8**. The Maven wrapper (`mvnw`) is gitignored.

## Project overview

Multi-module Maven educational codebase demonstrating Spring Boot 3.4.0 features. The root POM (`com.atguigu:springboot3:1.0`) is `packaging: pom` and only defines the `<modules>` list + `maven-compiler-plugin` (Java 21). Each of the 20 submodules is a **completely independent Spring Boot application** — every module has its own `spring-boot-starter-parent:3.4.0` parent, its own `main()` class, and its own `spring-boot-maven-plugin`. There are zero inter-module dependencies.

## Module map

| Module | What it covers |
|---|---|
| `boot3-01-demo` | Core boot: IoC container, YAML config binding (`@ConfigurationProperties`), AOP, bean lifecycle, Druid connection pool, Sa-Token auth, Redis, Actuator, GraalVM native-image plugin |
| `boot3-02-demo` | Spring MVC + Thymeleaf template rendering |
| `boot3-03-logging` | Logback logging configuration (log levels, file output) |
| `boot3-04-web` | Web MVC deep dive: `WebMvcConfigurer`, custom `HttpMessageConverter`, functional web (`RouterFunction`), `@ControllerAdvice` global exception handling |
| `boot3-05-ssm` | MyBatis XML mapper integration (SSM = Spring + SpringMVC + MyBatis) |
| `boot3-06-features` | Profile-based config (`application-{profile}.properties`), `@Profile`, `@ConditionalOnXxx`, multi-document YAML |
| `boot3-07-core` | Core patterns: Spring event publish/listen, `@EventListener`, `ApplicationListener`, service layer composition, `@Autowired` strategies |
| `boot3-08-robot-starter` | **Custom Spring Boot starter**: `AutoConfiguration.imports`, `@EnableRobot` annotation, `RobotProperties`, auto-configuration pattern |
| `boot3-09-redis` | Spring Data Redis with Lettuce driver, RedisTemplate, custom serialization config |
| `boot3-10-crud` | RESTful CRUD API + SpringDoc OpenAPI/Swagger UI documentation |
| `boot3-11-rpc` | Spring 6 **declarative HTTP interface** (`@HttpExchange`/`@GetExchange`), WebClient-based RPC calling external weather API |
| `boot3-12-message` | Spring Kafka: `KafkaTemplate` producer, `@KafkaListener` consumer |
| `boot3-13-security` | Spring Security with Thymeleaf login/logout, in-memory auth |
| `boot3-14-actuator` | Actuator endpoints + Micrometer Prometheus metrics export |
| `boot3-15-aot-common` | Plain Java project (no Spring Boot parent) — AOT compilation prerequisites |
| `boot3-16-aot-springboot` | Spring Boot AOT (Ahead-of-Time) compilation |
| `boot3-17-webflux` | Reactive stack: Reactor (Mono/Flux), WebFlux Router Functions, JDK9 Flow API, reactive MongoDB, SSE, async Servlets |
| `boot3-18-mybatis-plus` | MyBatis-Plus: ActiveRecord pattern, code generator, pagination, optimistic locking |
| `boot3-19-samples` | Design patterns with Spring: Strategy pattern (IoC), custom AOP annotations, Spring events, `@Transactional` with MyBatis-Plus |
| `boot3-20-rocketmq` | Apache RocketMQ: ordered messages, transactional messages |

## Key patterns

### Package naming
Three conventions coexist:
- `com.atguigu.boot` — early modules (01-demo, 02-demo)
- `com.atguigu.boot3.<topic>` — most intermediate modules (05-ssm through 14-actuator, 16-aot, 20-rocketmq)
- `com.serendipity` — boot3-17-webflux, boot3-18-mybatis-plus

### Custom starter structure (boot3-08-robot-starter)
The canonical custom starter pattern used: `AutoConfiguration` class registered in `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`, `@EnableXxx` annotation with `@Import`, `XxxProperties` for config binding, `XxxService` for business logic.

### Configuration
Each module uses `application.properties` (or `.yml`). Profile-specific config is demonstrated in `boot3-06-features` via `application-{dev,test,prod}.properties`. YAML multi-document syntax (`---`) is shown in `boot3-01-demo`.

### Data access
MyBatis XML mapper pattern is used in `boot3-05-ssm` (manual mapper XML). MyBatis-Plus is used in `boot3-18-mybatis-plus` (code-first with `BaseMapper`). Both use MySQL via `mysql-connector-j`.
