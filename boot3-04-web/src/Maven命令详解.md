# Maven 命令详解

## 一、Lifecycle (生命周期) 命令

Maven 生命周期是**按顺序执行**的，执行后面的命令会自动执行前面的所有阶段。

### 1. **clean**
- **作用**：清理项目，删除 `target` 目录及其所有内容
- **使用场景**：构建前清理旧的编译文件，避免缓存问题
- **执行内容**：删除所有生成的 `.class` 文件、JAR 包等

### 2. **validate**
- **作用**：验证项目是否正确，检查所有必要信息是否可用
- **使用场景**：检查 pom.xml 配置是否正确
- **执行内容**：验证 POM 文件格式、依赖声明等

### 3. **compile**
- **作用**：编译项目源代码
- **使用场景**：开发时检查代码是否有语法错误
- **执行内容**：
  - 自动执行：validate
  - 编译 `src/main/java` 下的所有 Java 文件
  - 生成 `.class` 文件到 `target/classes`
  - 复制 `src/main/resources` 资源文件到 `target/classes`

### 4. **test**
- **作用**：运行单元测试
- **使用场景**：执行项目的所有测试用例
- **执行内容**：
  - 自动执行：validate → compile
  - 编译 `src/test/java` 测试代码到 `target/test-classes`
  - 使用 Surefire 插件运行测试
  - 生成测试报告到 `target/surefire-reports`
- **注意**：测试失败会中断构建

### 5. **package**
- **作用**：打包编译后的代码
- **使用场景**：生成可分发的格式（JAR/WAR）
- **执行内容**：
  - 自动执行：validate → compile → test
  - 将编译好的代码打包成 JAR 或 WAR
  - 生成 `boot3-04-web-0.0.1-SNAPSHOT.jar` 到 `target` 目录
  - Spring Boot 会生成可执行的 Fat JAR（包含所有依赖）

### 6. **verify**
- **作用**：运行检查以验证包是否有效且符合质量标准
- **使用场景**：集成测试、代码质量检查
- **执行内容**：
  - 自动执行：validate → compile → test → package
  - 运行集成测试
  - 执行代码质量检查插件（如有配置）

### 7. **install**
- **作用**：将包安装到本地 Maven 仓库
- **使用场景**：供本机其他项目引用
- **执行内容**：
  - 自动执行：validate → compile → test → package → verify
  - 将 JAR 安装到 `~/.m2/repository/com/atguigu/boot3-04-web/...`
  - 其他本地项目可以通过依赖坐标引用

### 8. **site**
- **作用**：生成项目站点文档
- **使用场景**：生成项目报告、JavaDoc 等
- **执行内容**：
  - 生成项目的 HTML 文档
  - 包括 JavaDoc、测试报告、依赖关系等

### 9. **deploy**
- **作用**：将最终包发布到远程仓库
- **使用场景**：发布到公司私服或 Maven 中央仓库
- **执行内容**：
  - 自动执行：validate → compile → test → package → verify → install
  - 上传 JAR 到配置的远程仓库（如 Nexus）
  - 供团队其他成员或其他服务器下载使用

---

## 二、Plugins (插件) 命令

插件提供**具体功能**，可以独立执行，不一定按生命周期顺序。

### 1. **clean** (maven-clean-plugin:3.2.0)
- **作用**：清理插件
- **Goal**: `clean:clean`
- **功能**：删除 target 目录
- **与 Lifecycle clean 关系**：Lifecycle 的 clean 阶段会调用这个插件

### 2. **compiler** (maven-compiler-plugin:3.10.1)
- **作用**：Java 编译插件
- **常用 Goals**:
  - `compiler:compile` - 编译主代码
  - `compiler:testCompile` - 编译测试代码
- **功能**：使用 Java 21 编译器编译代码

### 3. **deploy** (maven-deploy-plugin:3.0.0)
- **作用**：部署插件
- **Goal**: `deploy:deploy`
- **功能**：上传构建产物到远程仓库

### 4. **install** (maven-install-plugin:3.0.1)
- **作用**：安装插件
- **Goal**: `install:install`
- **功能**：安装构建产物到本地仓库

### 5. **jar** (maven-jar-plugin:3.3.0)
- **作用**：JAR 打包插件
- **Goal**: `jar:jar`
- **功能**：打包项目为 JAR 文件
- **Spring Boot 增强**：会被 spring-boot-maven-plugin 增强

### 6. **resources** (maven-resources-plugin:3.3.1)
- **作用**：资源文件处理插件
- **常用 Goals**:
  - `resources:resources` - 复制主资源文件
  - `resources:testResources` - 复制测试资源文件
- **功能**：复制 `src/main/resources` 到 `target/classes`

### 7. **site** (maven-site-plugin:3.12.1)
- **作用**：站点生成插件
- **Goal**: `site:site`
- **功能**：生成项目文档和报告

### 8. **spring-boot** (spring-boot-maven-plugin:3.0.6)
- **作用**：Spring Boot 专用插件 ⭐
- **重要 Goals**:
  - `spring-boot:run` - 直接运行 Spring Boot 应用（开发常用）
  - `spring-boot:repackage` - 重新打包为可执行 JAR
  - `spring-boot:build-image` - 构建 Docker 镜像
- **功能**：
  - 创建包含所有依赖的 Fat JAR
  - 支持热部署（配合 devtools）
  - 快速启动应用进行测试

### 9. **surefire** (maven-surefire-plugin:2.22.2)
- **作用**：单元测试插件
- **Goal**: `surefire:test`
- **功能**：
  - 运行 JUnit、TestNG 测试
  - 生成测试报告
  - 支持并发执行测试

---

## 三、常用命令组合

### 开发阶段
```bash
# 清理并编译
mvn clean compile

# 清理、编译、运行测试
mvn clean test

# 清理并打包（最常用）
mvn clean package

# 直接运行 Spring Boot 应用（开发时最常用）
mvn spring-boot:run
```

### 部署阶段
```bash
# 清理、测试、打包、安装到本地仓库
mvn clean install

# 跳过测试快速打包
mvn clean package -DskipTests

# 发布到远程仓库
mvn clean deploy
```

### 快速操作
```bash
# 只编译不测试
mvn compile

# 只运行测试
mvn test

# 只打包（前提是已经 compile 过）
mvn package
```

---

## 四、执行顺序对比

### Lifecycle 命令（自动包含前面的阶段）
```
clean          → 只清理
compile        → validate + compile
test           → validate + compile + test
package        → validate + compile + test + package
install        → validate + compile + test + package + verify + install
deploy         → validate + compile + test + package + verify + install + deploy
```

### Plugin 命令（独立执行）
```
spring-boot:run        → 直接运行，不打包
compiler:compile       → 只编译，不执行其他阶段
surefire:test         → 只测试，但需要先编译
jar:jar               → 只打包，不测试
```

---

## 五、关键区别总结

| 对比项 | Lifecycle | Plugins |
|-------|-----------|---------|
| **性质** | 抽象阶段序列 | 具体执行工具 |
| **执行方式** | 顺序执行多个阶段 | 执行特定 Goal |
| **依赖性** | 后面的包含前面的 | 可独立执行 |
| **可见性** | 标准化，所有项目一致 | 取决于 POM 配置 |
| **示例** | `mvn clean install` | `mvn spring-boot:run` |

---

## 六、在 IDEA 中的使用建议

### 日常开发
1. **运行应用**：双击 `spring-boot:run`（最快）
2. **编译检查**：双击 `compile`
3. **运行测试**：双击 `test`

### 打包部署
1. **本地打包**：双击 `clean` + `package`
2. **安装到本地仓库**：双击 `install`
3. **发布**：双击 `deploy`

### 清理问题
- 遇到奇怪的编译问题，先执行 `clean`，再执行其他命令

---

## 七、本项目特别说明

根据 `boot3-04-web` 的 POM 配置：
- **Java 版本**：Java 21
- **Spring Boot 版本**：3.0.6
- **打包类型**：JAR（可执行 Fat JAR）
- **主要依赖**：
  - spring-boot-starter-web（Web 应用）
  - spring-boot-starter-thymeleaf（模板引擎）
  - spring-boot-devtools（热部署）
  - 自定义 starter：boot3-08-robot-starter

**最常用命令**：
```bash
# 开发调试
mvn spring-boot:run

# 打包测试
mvn clean package

# 运行打包后的 JAR
java -jar target/boot3-04-web-0.0.1-SNAPSHOT.jar
```

