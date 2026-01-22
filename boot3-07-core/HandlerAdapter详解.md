# HandlerAdapter 详解

## 什么是 HandlerAdapter？

`HandlerAdapter` 是 Spring MVC 框架中的一个核心接口，它是**适配器模式**的经典应用。它的主要作用是**适配不同类型的 Handler（控制器），提供统一的调用方式**。

---

## 为什么需要 HandlerAdapter？

### 问题背景

在 Spring MVC 中，Handler（处理器）可以有多种形式：

1. **注解方式**：使用 `@RequestMapping` 的方法
   ```java
   @Controller
   public class UserController {
       @GetMapping("/user/{id}")
       public String getUser(@PathVariable Long id) {
           return "userDetail";
       }
   }
   ```

2. **实现 Controller 接口**：
   ```java
   public class OldStyleController implements Controller {
       @Override
       public ModelAndView handleRequest(HttpServletRequest request, 
                                        HttpServletResponse response) {
           return new ModelAndView("view");
       }
   }
   ```

3. **实现 HttpRequestHandler 接口**：
   ```java
   public class MyHttpRequestHandler implements HttpRequestHandler {
       @Override
       public void handleRequest(HttpServletRequest request, 
                                HttpServletResponse response) {
           // 直接操作 response
       }
   }
   ```

4. **函数式编程**：
   ```java
   @Bean
   public RouterFunction<ServerResponse> route() {
       return RouterFunctions.route()
           .GET("/api/user", request -> ServerResponse.ok().build())
           .build();
   }
   ```

### 核心问题

这些不同类型的 Handler：
- **方法签名不同**：有的返回 String，有的返回 ModelAndView，有的无返回值
- **参数不同**：有的接收 HttpServletRequest/Response，有的接收解析后的参数
- **调用方式不同**：有的需要反射调用方法，有的直接调用接口方法

如果在 `DispatcherServlet` 中针对每种类型写不同的处理逻辑，代码会变得：
- 难以维护
- 难以扩展
- 违反开闭原则

### 解决方案：适配器模式

`HandlerAdapter` 就是为了解决这个问题而设计的：
- **统一接口**：为所有类型的 Handler 提供统一的调用接口
- **解耦**：DispatcherServlet 不需要知道 Handler 的具体类型
- **可扩展**：添加新类型的 Handler 只需实现新的 Adapter

---

## HandlerAdapter 接口定义

```java
public interface HandlerAdapter {
    
    /**
     * 判断此适配器是否支持给定的 Handler
     * @param handler 要检查的 Handler 对象
     * @return 如果此适配器可以处理该 Handler，返回 true
     */
    boolean supports(Object handler);
    
    /**
     * 使用给定的 Handler 处理请求
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler 要使用的 Handler 对象
     * @return ModelAndView 对象（如果 Handler 自己处理响应则返回 null）
     * @throws Exception 如果处理失败
     */
    @Nullable
    ModelAndView handle(HttpServletRequest request, 
                       HttpServletResponse response, 
                       Object handler) throws Exception;
    
    /**
     * 与 HTTP 缓存支持相关的方法
     * 返回请求资源的最后修改时间
     * @param request 当前 HTTP 请求
     * @param handler 要使用的 Handler 对象
     * @return 最后修改时间戳，如果不支持则返回 -1
     */
    long getLastModified(HttpServletRequest request, Object handler);
}
```

---

## 在 doDispatch 中的作用

让我们回顾 `DispatcherServlet.doDispatch()` 方法中如何使用 HandlerAdapter：

```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) 
        throws Exception {
    
    // ... 省略前面的代码 ...
    
    // 步骤1：根据请求找到对应的 Handler（比如 UserController.getUser 方法）
    mappedHandler = getHandler(processedRequest);
    if (mappedHandler == null) {
        noHandlerFound(processedRequest, response);
        return;
    }
    
    // 步骤2：根据 Handler 找到对应的 HandlerAdapter【关键步骤】
    HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
    
    // ... 省略中间的代码 ...
    
    // 步骤3：通过 HandlerAdapter 调用 Handler【统一调用】
    mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
    
    // ... 省略后续的代码 ...
}
```

### 详细分析步骤2：getHandlerAdapter()

```java
protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
    if (this.handlerAdapters != null) {
        // 遍历所有的 HandlerAdapter
        for (HandlerAdapter adapter : this.handlerAdapters) {
            // 找到第一个支持该 Handler 的适配器
            if (adapter.supports(handler)) {
                return adapter;
            }
        }
    }
    throw new ServletException("No adapter for handler [" + handler +
            "]: The DispatcherServlet configuration needs to include a HandlerAdapter");
}
```

**工作流程**：
1. 遍历所有已注册的 `HandlerAdapter`
2. 调用每个适配器的 `supports()` 方法
3. 返回第一个返回 `true` 的适配器
4. 如果没有找到适配器，抛出异常

---

## Spring MVC 中的 HandlerAdapter 实现

### 1. RequestMappingHandlerAdapter（最常用）

**支持的 Handler 类型**：
- 带有 `@RequestMapping` 注解的方法
- 包括 `@GetMapping`、`@PostMapping` 等派生注解

**示例**：
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id, 
                       @RequestParam(required = false) String fields) {
        return userService.getUser(id);
    }
    
    @PostMapping
    public User createUser(@RequestBody @Valid UserDto dto) {
        return userService.createUser(dto);
    }
}
```

**核心功能**：
1. **参数解析**
   - `@PathVariable`：路径变量
   - `@RequestParam`：请求参数
   - `@RequestBody`：请求体（JSON/XML）
   - `@RequestHeader`：请求头
   - `@CookieValue`：Cookie
   - `HttpServletRequest/Response`：原生对象
   - `Model/ModelMap`：模型对象
   - 等等...

2. **数据绑定**
   - 自动将请求数据绑定到方法参数
   - 支持类型转换（String → Integer、Date 等）
   - 支持数据验证（JSR-303）

3. **返回值处理**
   - `String`：视图名称
   - `ModelAndView`：包含视图和模型
   - `@ResponseBody`：直接写入响应体（JSON/XML）
   - `ResponseEntity`：包含状态码和响应体
   - `void`：表示已经处理了响应
   - 等等...

**内部处理流程**：
```
请求到达
    ↓
参数解析器链（HandlerMethodArgumentResolver）
    ├── PathVariableMethodArgumentResolver  处理 @PathVariable
    ├── RequestParamMethodArgumentResolver  处理 @RequestParam
    ├── RequestBodyMethodArgumentResolver   处理 @RequestBody
    └── ... 其他解析器
    ↓
调用 Controller 方法（通过反射）
    ↓
返回值处理器链（HandlerMethodReturnValueHandler）
    ├── ModelAndViewMethodReturnValueHandler  处理 ModelAndView
    ├── ViewNameMethodReturnValueHandler      处理 String
    ├── RequestResponseBodyMethodProcessor    处理 @ResponseBody
    └── ... 其他处理器
    ↓
返回 ModelAndView
```

---

### 2. HttpRequestHandlerAdapter

**支持的 Handler 类型**：
- 实现了 `HttpRequestHandler` 接口的类

**示例**：
```java
@Component("/legacy/handler")
public class LegacyHttpRequestHandler implements HttpRequestHandler {
    
    @Override
    public void handleRequest(HttpServletRequest request, 
                             HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter writer = response.getWriter();
        writer.write("<h1>Hello from HttpRequestHandler</h1>");
        writer.flush();
    }
}
```

**特点**：
- 直接操作原生 Servlet API
- 没有返回值（void）
- 适合需要完全控制响应的场景
- 性能较好（无额外处理）

**supports() 实现**：
```java
@Override
public boolean supports(Object handler) {
    return (handler instanceof HttpRequestHandler);
}
```

**handle() 实现**：
```java
@Override
public ModelAndView handle(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object handler) throws Exception {
    ((HttpRequestHandler) handler).handleRequest(request, response);
    return null;  // 表示已经处理了响应，不需要视图渲染
}
```

---

### 3. SimpleControllerHandlerAdapter

**支持的 Handler 类型**：
- 实现了 `Controller` 接口的类

**示例**：
```java
@Component("/legacy/controller")
public class LegacyController implements Controller {
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, 
                                     HttpServletResponse response) 
            throws Exception {
        
        ModelAndView mv = new ModelAndView("legacyView");
        mv.addObject("message", "Hello from old-style Controller");
        return mv;
    }
}
```

**特点**：
- Spring 1.x/2.x 时代的遗留风格
- 返回 ModelAndView
- 每个 Controller 只能处理一个 URL
- 现在已很少使用

**supports() 实现**：
```java
@Override
public boolean supports(Object handler) {
    return (handler instanceof Controller);
}
```

**handle() 实现**：
```java
@Override
public ModelAndView handle(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object handler) throws Exception {
    return ((Controller) handler).handleRequest(request, response);
}
```

---

### 4. SimpleServletHandlerAdapter

**支持的 Handler 类型**：
- 实现了 `Servlet` 接口的类

**示例**：
```java
@Component("/legacy/servlet")
public class LegacyServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        resp.getWriter().write("Hello from Servlet");
    }
}
```

**特点**：
- 将 Servlet 集成到 Spring MVC 中
- 完全按照 Servlet 规范工作
- 适合集成遗留的 Servlet 代码

---

## HandlerAdapter 的工作原理图

```
┌─────────────────────────────────────────────────────────────┐
│                    DispatcherServlet                         │
│                                                              │
│  1. getHandler()  ──→  获取 Handler（如 UserController 的方法） │
│                                                              │
│  2. getHandlerAdapter()  ──→  查找合适的适配器                  │
│        │                                                     │
│        ├──→ RequestMappingHandlerAdapter.supports()  ✓      │
│        │    (支持 @RequestMapping 注解的方法)                  │
│        │                                                     │
│        ├──→ HttpRequestHandlerAdapter.supports()    ✗      │
│        │    (不支持，不是 HttpRequestHandler)                 │
│        │                                                     │
│        └──→ SimpleControllerHandlerAdapter.supports() ✗    │
│             (不支持，不是 Controller 接口)                     │
│                                                              │
│  3. ha.handle()  ──→  通过适配器调用 Handler                  │
│        │                                                     │
│        └──→ RequestMappingHandlerAdapter.handle()          │
│               │                                              │
│               ├─→ 解析方法参数（@PathVariable, @RequestParam...）│
│               │                                              │
│               ├─→ 调用 Controller 方法（反射）                  │
│               │                                              │
│               ├─→ 处理返回值（@ResponseBody, ModelAndView...）│
│               │                                              │
│               └─→ 返回 ModelAndView                          │
│                                                              │
│  4. 渲染视图或返回 JSON                                        │
└─────────────────────────────────────────────────────────────┘
```

---

## RequestMappingHandlerAdapter 详细工作流程

作为最常用的适配器，`RequestMappingHandlerAdapter` 的工作流程值得深入了解：

### 1. 参数解析阶段

```java
// Controller 方法示例
@PostMapping("/users")
public User createUser(
    @RequestBody @Valid UserDto dto,           // 参数1
    @RequestHeader("Authorization") String token,  // 参数2
    @RequestParam(required = false) String lang,   // 参数3
    HttpServletRequest request                     // 参数4
) {
    // ...
}
```

**解析流程**：
```java
public class RequestMappingHandlerAdapter {
    
    private List<HandlerMethodArgumentResolver> argumentResolvers;
    
    public ModelAndView handle(...) {
        // 获取 Controller 方法的所有参数
        MethodParameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        
        // 遍历每个参数
        for (int i = 0; i < parameters.length; i++) {
            MethodParameter parameter = parameters[i];
            
            // 找到支持该参数的解析器
            for (HandlerMethodArgumentResolver resolver : argumentResolvers) {
                if (resolver.supportsParameter(parameter)) {
                    // 使用解析器解析参数值
                    args[i] = resolver.resolveArgument(
                        parameter, request, response, ...
                    );
                    break;
                }
            }
        }
        
        // 调用方法
        Object result = method.invoke(handler, args);
        
        // ... 处理返回值 ...
    }
}
```

**常见的参数解析器**：

| 解析器 | 支持的参数类型 | 示例 |
|-------|-------------|------|
| `PathVariableMethodArgumentResolver` | `@PathVariable` | `@PathVariable Long id` |
| `RequestParamMethodArgumentResolver` | `@RequestParam` | `@RequestParam String name` |
| `RequestBodyMethodArgumentResolver` | `@RequestBody` | `@RequestBody UserDto dto` |
| `RequestHeaderMethodArgumentResolver` | `@RequestHeader` | `@RequestHeader String token` |
| `CookieValueMethodArgumentResolver` | `@CookieValue` | `@CookieValue String sessionId` |
| `ServletRequestMethodArgumentResolver` | `HttpServletRequest/Response` | `HttpServletRequest request` |
| `ModelMethodProcessor` | `Model/ModelMap` | `Model model` |

### 2. 数据绑定和验证

```java
@PostMapping("/users")
public User createUser(@RequestBody @Valid UserDto dto) {
    // dto 已经完成了：
    // 1. JSON 反序列化（通过 Jackson/Gson）
    // 2. 数据验证（通过 JSR-303）
}

// DTO 类
@Data
public class UserDto {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    @Min(value = 18, message = "年龄必须大于18")
    private Integer age;
}
```

**验证流程**：
1. `RequestBodyMethodArgumentResolver` 解析 `@RequestBody`
2. 使用 `HttpMessageConverter` 反序列化 JSON
3. 如果有 `@Valid` 注解，执行验证
4. 验证失败抛出 `MethodArgumentNotValidException`
5. 可以通过 `@ExceptionHandler` 捕获并处理

### 3. 方法调用阶段

```java
// 通过反射调用 Controller 方法
Method method = ...; // Controller 的方法对象
Object handler = ...; // Controller 实例
Object[] args = ...; // 解析好的参数数组

Object result = method.invoke(handler, args);
```

### 4. 返回值处理阶段

```java
// Controller 方法的各种返回值示例
@Controller
public class UserController {
    
    // 返回值类型1：String（视图名称）
    @GetMapping("/user/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.getUser(id));
        return "userDetail";  // 视图名称
    }
    
    // 返回值类型2：ModelAndView
    @GetMapping("/user/list")
    public ModelAndView listUsers() {
        ModelAndView mv = new ModelAndView("userList");
        mv.addObject("users", userService.getAllUsers());
        return mv;
    }
    
    // 返回值类型3：@ResponseBody（直接返回数据）
    @GetMapping("/api/user/{id}")
    @ResponseBody
    public User getUserApi(@PathVariable Long id) {
        return userService.getUser(id);  // 自动序列化为 JSON
    }
    
    // 返回值类型4：ResponseEntity（包含状态码）
    @PostMapping("/api/user")
    public ResponseEntity<User> createUser(@RequestBody UserDto dto) {
        User user = userService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
    
    // 返回值类型5：void（自己处理响应）
    @GetMapping("/download")
    public void downloadFile(HttpServletResponse response) throws IOException {
        response.setContentType("application/octet-stream");
        // 写入文件内容...
    }
}
```

**常见的返回值处理器**：

| 处理器 | 支持的返回值类型 | 处理方式 |
|-------|---------------|---------|
| `ViewNameMethodReturnValueHandler` | `String` | 作为视图名称 |
| `ModelAndViewMethodReturnValueHandler` | `ModelAndView` | 包含视图和模型 |
| `RequestResponseBodyMethodProcessor` | `@ResponseBody` | 序列化为 JSON/XML |
| `HttpEntityMethodProcessor` | `ResponseEntity` | 设置状态码和响应体 |
| `ServletModelAttributeMethodProcessor` | 普通对象 | 作为模型属性 |

---

## 适配器模式的优势

### 1. 解耦
- `DispatcherServlet` 不需要知道 Handler 的具体类型
- 只需要知道 `HandlerAdapter` 接口

### 2. 扩展性
- 添加新类型的 Handler，只需实现新的 Adapter
- 不需要修改 `DispatcherServlet` 的代码

### 3. 统一性
- 所有 Handler 都通过 `handle()` 方法调用
- 返回统一的 `ModelAndView` 对象

### 4. 灵活性
- 每个 Adapter 可以有自己的处理逻辑
- 可以添加额外的功能（参数解析、数据验证等）

---

## 自定义 HandlerAdapter 示例

```java
// 1. 定义自定义的 Handler 接口
public interface JsonHandler {
    Object handleJson(JSONObject json);
}

// 2. 实现 Handler
@Component
public class MyJsonHandler implements JsonHandler {
    @Override
    public Object handleJson(JSONObject json) {
        // 处理 JSON 请求
        return Map.of("status", "success", "data", json);
    }
}

// 3. 实现自定义的 HandlerAdapter
@Component
public class JsonHandlerAdapter implements HandlerAdapter {
    
    @Override
    public boolean supports(Object handler) {
        // 判断是否支持该 Handler
        return handler instanceof JsonHandler;
    }
    
    @Override
    public ModelAndView handle(HttpServletRequest request, 
                              HttpServletResponse response, 
                              Object handler) throws Exception {
        
        // 1. 读取请求体
        String body = IOUtils.toString(request.getInputStream(), "UTF-8");
        JSONObject json = JSON.parseObject(body);
        
        // 2. 调用 Handler
        JsonHandler jsonHandler = (JsonHandler) handler;
        Object result = jsonHandler.handleJson(json);
        
        // 3. 写入响应
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(JSON.toJSONString(result));
        
        // 4. 返回 null 表示已经处理了响应
        return null;
    }
    
    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;  // 不支持缓存
    }
}

// 4. 注册 HandlerMapping（让 Spring MVC 能找到 Handler）
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Bean
    public SimpleUrlHandlerMapping jsonHandlerMapping() {
        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        Map<String, Object> urlMap = new HashMap<>();
        urlMap.put("/api/json/**", myJsonHandler());
        mapping.setUrlMap(urlMap);
        return mapping;
    }
    
    @Bean
    public MyJsonHandler myJsonHandler() {
        return new MyJsonHandler();
    }
}
```

---

## 总结

### HandlerAdapter 的核心价值

1. **适配器模式的应用**
   - 将不同类型的 Handler 适配到统一的接口
   - DispatcherServlet 只需要与 HandlerAdapter 交互

2. **支持多种 Handler 类型**
   - `@RequestMapping` 注解的方法（最常用）
   - 实现 `Controller` 接口的类（遗留）
   - 实现 `HttpRequestHandler` 接口的类
   - 实现 `Servlet` 接口的类

3. **强大的功能**
   - 参数自动解析和绑定
   - 数据验证
   - 类型转换
   - 返回值处理
   - 异常处理

4. **良好的扩展性**
   - 可以自定义 HandlerAdapter
   - 可以自定义参数解析器
   - 可以自定义返回值处理器

### 关键要点

✅ **HandlerAdapter 是做什么的？**  
→ 适配不同类型的 Handler，提供统一的调用方式

✅ **为什么需要 HandlerAdapter？**  
→ 因为 Handler 有多种类型，调用方式不同，需要适配器统一处理

✅ **最常用的 HandlerAdapter 是哪个？**  
→ `RequestMappingHandlerAdapter`，处理 `@RequestMapping` 注解

✅ **HandlerAdapter 在 doDispatch 中的作用？**  
→ 作为中间层，将 DispatcherServlet 和 Handler 解耦，统一调用接口

✅ **如何找到合适的 HandlerAdapter？**  
→ 遍历所有 Adapter，调用 `supports()` 方法，返回第一个支持的

---

## 扩展阅读

- [Spring MVC 官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html)
- [DispatcherServlet 源码分析](https://github.com/spring-projects/spring-framework/blob/main/spring-webmvc/src/main/java/org/springframework/web/servlet/DispatcherServlet.java)
- [适配器模式详解](https://refactoring.guru/design-patterns/adapter)

