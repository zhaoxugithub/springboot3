# DispatcherServlet 的 doDispatch 方法详解

## 概述

`doDispatch` 是 Spring MVC 框架中 `DispatcherServlet` 类的核心方法，负责处理所有进入的 HTTP 请求。它是整个请求处理流程的中央调度器。

## doDispatch 方法源码分析

```java
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HttpServletRequest processedRequest = request;
    HandlerExecutionChain mappedHandler = null;
    boolean multipartRequestParsed = false;
    
    WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);
    
    try {
        ModelAndView mv = null;
        Exception dispatchException = null;
        
        try {
            // 1. 检查是否是文件上传请求
            processedRequest = checkMultipart(request);
            multipartRequestParsed = (processedRequest != request);
            
            // 2. 根据请求找到对应的 Handler（Controller 方法）
            mappedHandler = getHandler(processedRequest);
            if (mappedHandler == null) {
                noHandlerFound(processedRequest, response);
                return;
            }
            
            // 3. 根据 Handler 找到对应的 HandlerAdapter
            HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
            
            // 4. 处理 GET、HEAD 请求的 Last-Modified 头
            String method = request.getMethod();
            boolean isGet = HttpMethod.GET.matches(method);
            if (isGet || HttpMethod.HEAD.matches(method)) {
                long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
                if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
                    return;
                }
            }
            
            // 5. 执行拦截器的 preHandle 方法
            if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                return;
            }
            
            // 6. 实际调用 Handler（Controller 方法）
            mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
            
            // 7. 处理异步请求
            if (asyncManager.isConcurrentHandlingStarted()) {
                return;
            }
            
            // 8. 如果没有视图名称，应用默认视图名称
            applyDefaultViewName(processedRequest, mv);
            
            // 9. 执行拦截器的 postHandle 方法
            mappedHandler.applyPostHandle(processedRequest, response, mv);
        }
        catch (Exception ex) {
            dispatchException = ex;
        }
        catch (Throwable err) {
            dispatchException = new NestedServletException("Handler dispatch failed", err);
        }
        
        // 10. 处理执行结果（渲染视图或处理异常）
        processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
    }
    catch (Exception ex) {
        // 11. 触发拦截器的 afterCompletion 方法
        triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
    }
    catch (Throwable err) {
        triggerAfterCompletion(processedRequest, response, mappedHandler,
                new NestedServletException("Handler processing failed", err));
    }
    finally {
        // 12. 清理异步请求和文件上传资源
        if (asyncManager.isConcurrentHandlingStarted()) {
            if (mappedHandler != null) {
                mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
            }
        }
        else {
            if (multipartRequestParsed) {
                cleanupMultipart(processedRequest);
            }
        }
    }
}
```

## 详细步骤解析

### 1. 检查是否是文件上传请求 (checkMultipart)

```java
processedRequest = checkMultipart(request);
multipartRequestParsed = (processedRequest != request);
```

**作用：**
- 检查请求的 Content-Type 是否是 `multipart/form-data`
- 如果是文件上传请求，将原始请求包装成 `MultipartHttpServletRequest`
- 这样后续可以方便地处理文件上传

**关键点：**
- 使用 `MultipartResolver` 来解析文件上传请求
- 如果不是文件上传，返回原始请求对象
- `multipartRequestParsed` 标记用于后续清理资源

---

### 2. 获取处理器 (getHandler)

```java
mappedHandler = getHandler(processedRequest);
if (mappedHandler == null) {
    noHandlerFound(processedRequest, response);
    return;
}
```

**作用：**
- 遍历所有的 `HandlerMapping`（处理器映射器）
- 根据请求 URL、请求方法等信息找到对应的 Handler（通常是 Controller 中的方法）
- 返回 `HandlerExecutionChain` 对象，包含 Handler 和拦截器链

**处理器映射器的类型：**
- `RequestMappingHandlerMapping`：处理 @RequestMapping 注解
- `BeanNameUrlHandlerMapping`：根据 bean 名称映射
- `SimpleUrlHandlerMapping`：简单 URL 映射

**如果找不到 Handler：**
- 调用 `noHandlerFound()` 方法
- 返回 404 错误或抛出 `NoHandlerFoundException`

---

### 3. 获取处理器适配器 (getHandlerAdapter)

```java
HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());
```

**作用：**
- 根据 Handler 类型找到对应的 `HandlerAdapter`（处理器适配器）
- 适配器模式：统一不同类型 Handler 的调用方式

**常见的适配器类型：**
- `RequestMappingHandlerAdapter`：处理 @RequestMapping 注解的方法
- `HttpRequestHandlerAdapter`：处理实现了 HttpRequestHandler 接口的 Handler
- `SimpleControllerHandlerAdapter`：处理实现了 Controller 接口的 Handler

**为什么需要适配器？**
- Handler 可以是多种类型（方法、类、函数式接口等）
- 适配器提供统一的调用接口
- 解耦 DispatcherServlet 和具体的 Handler 实现

---

### 4. 处理 Last-Modified 头

```java
String method = request.getMethod();
boolean isGet = HttpMethod.GET.matches(method);
if (isGet || HttpMethod.HEAD.matches(method)) {
    long lastModified = ha.getLastModified(request, mappedHandler.getHandler());
    if (new ServletWebRequest(request, response).checkNotModified(lastModified) && isGet) {
        return;
    }
}
```

**作用：**
- 处理 HTTP 缓存机制
- 对于 GET 和 HEAD 请求，检查资源是否被修改
- 如果资源未修改，返回 304 状态码，减少网络传输

**工作原理：**
1. 获取资源的最后修改时间
2. 与请求头中的 `If-Modified-Since` 进行比较
3. 如果资源未修改，设置 304 状态码并直接返回
4. 如果资源已修改，继续处理请求

---

### 5. 执行拦截器的 preHandle 方法

```java
if (!mappedHandler.applyPreHandle(processedRequest, response)) {
    return;
}
```

**作用：**
- 按顺序执行所有拦截器的 `preHandle` 方法
- 在实际调用 Handler 之前进行预处理

**拦截器的用途：**
- 权限验证
- 日志记录
- 参数验证
- 性能监控

**执行流程：**
```java
for (int i = 0; i < interceptors.length; i++) {
    HandlerInterceptor interceptor = interceptors[i];
    if (!interceptor.preHandle(request, response, handler)) {
        triggerAfterCompletion(request, response, null);
        return false;
    }
}
```

**返回值含义：**
- 返回 `true`：继续执行后续拦截器和 Handler
- 返回 `false`：中断请求处理，不再执行后续操作

---

### 6. 实际调用 Handler (handle)

```java
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```

**作用：**
- 这是整个流程的核心步骤
- 通过适配器调用实际的 Controller 方法
- 返回 `ModelAndView` 对象

**内部处理流程（以 RequestMappingHandlerAdapter 为例）：**

1. **参数解析**
   - 使用 `HandlerMethodArgumentResolver` 解析方法参数
   - 支持多种参数类型：`@RequestParam`、`@PathVariable`、`@RequestBody` 等

2. **数据绑定**
   - 将请求参数绑定到方法参数
   - 执行数据验证（JSR-303 校验）

3. **调用方法**
   - 通过反射调用 Controller 方法
   - 传入解析好的参数

4. **处理返回值**
   - 使用 `HandlerMethodReturnValueHandler` 处理返回值
   - 支持多种返回类型：`ModelAndView`、`String`、`@ResponseBody` 等

**示例：**
```java
@GetMapping("/user/{id}")
public ModelAndView getUser(@PathVariable Long id) {
    User user = userService.getUser(id);
    ModelAndView mv = new ModelAndView("userDetail");
    mv.addObject("user", user);
    return mv;
}
```

---

### 7. 处理异步请求

```java
if (asyncManager.isConcurrentHandlingStarted()) {
    return;
}
```

**作用：**
- 检查是否启动了异步处理
- 如果是异步请求，直接返回，不继续后续流程

**异步处理场景：**
```java
@GetMapping("/async")
public Callable<String> asyncMethod() {
    return () -> {
        // 异步执行的业务逻辑
        Thread.sleep(1000);
        return "result";
    };
}
```

**异步支持类型：**
- `Callable`
- `DeferredResult`
- `CompletableFuture`
- `WebAsyncTask`

---

### 8. 应用默认视图名称

```java
applyDefaultViewName(processedRequest, mv);
```

**作用：**
- 如果 ModelAndView 中没有设置视图名称，应用默认视图名称
- 通常基于请求路径生成视图名称

**示例：**
```java
@GetMapping("/user/list")
public ModelAndView listUsers(ModelAndView mv) {
    mv.addObject("users", userService.getAllUsers());
    // 没有设置视图名称
    return mv;
}
// 默认视图名称为 "user/list"
```

---

### 9. 执行拦截器的 postHandle 方法

```java
mappedHandler.applyPostHandle(processedRequest, response, mv);
```

**作用：**
- Controller 方法执行完成后，按逆序执行拦截器的 `postHandle` 方法
- 可以修改 ModelAndView 对象

**执行流程：**
```java
for (int i = interceptors.length - 1; i >= 0; i--) {
    HandlerInterceptor interceptor = interceptors[i];
    interceptor.postHandle(request, response, handler, mv);
}
```

**典型用途：**
- 添加公共的模型数据
- 修改视图名称
- 添加额外的响应头

**注意：**
- 如果 Controller 方法抛出异常，`postHandle` 不会执行
- 按照与 `preHandle` 相反的顺序执行

---

### 10. 处理执行结果 (processDispatchResult)

```java
processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
```

**作用：**
- 渲染视图或处理异常
- 这是请求处理的最后阶段

**处理流程：**

#### a. 如果有异常，处理异常

```java
if (exception != null) {
    if (exception instanceof ModelAndViewDefiningException) {
        mv = ((ModelAndViewDefiningException) exception).getModelAndView();
    }
    else {
        Object handler = (mappedHandler != null ? mappedHandler.getHandler() : null);
        mv = processHandlerException(request, response, handler, exception);
    }
}
```

**异常处理机制：**
1. 遍历所有的 `HandlerExceptionResolver`
2. 尝试解析异常，返回 ModelAndView
3. 常见的异常解析器：
   - `ExceptionHandlerExceptionResolver`：处理 @ExceptionHandler 注解
   - `ResponseStatusExceptionResolver`：处理 @ResponseStatus 注解
   - `DefaultHandlerExceptionResolver`：处理 Spring MVC 标准异常

#### b. 渲染视图

```java
if (mv != null && !mv.wasCleared()) {
    render(mv, request, response);
}
```

**渲染过程：**
1. 解析视图名称，获取 View 对象
2. 使用 `ViewResolver` 解析视图
3. 调用 View 的 render 方法渲染视图
4. 将模型数据合并到请求属性中
5. 执行实际的视图渲染（JSP、Thymeleaf 等）

**视图解析器类型：**
- `InternalResourceViewResolver`：JSP 视图
- `ThymeleafViewResolver`：Thymeleaf 模板
- `FreeMarkerViewResolver`：FreeMarker 模板
- `ContentNegotiatingViewResolver`：内容协商视图

---

### 11. 触发拦截器的 afterCompletion 方法

```java
triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
```

**作用：**
- 无论请求是否成功，都会执行
- 按逆序执行拦截器的 `afterCompletion` 方法
- 用于资源清理

**执行时机：**
- 在视图渲染完成后执行
- 即使发生异常也会执行

**典型用途：**
- 清理资源（关闭连接、释放锁等）
- 记录请求完成日志
- 性能统计

**执行流程：**
```java
for (int i = this.interceptorIndex; i >= 0; i--) {
    HandlerInterceptor interceptor = interceptors[i];
    try {
        interceptor.afterCompletion(request, response, handler, ex);
    }
    catch (Throwable ex2) {
        logger.error("HandlerInterceptor.afterCompletion threw exception", ex2);
    }
}
```

---

### 12. 清理资源

```java
finally {
    if (asyncManager.isConcurrentHandlingStarted()) {
        if (mappedHandler != null) {
            mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
        }
    }
    else {
        if (multipartRequestParsed) {
            cleanupMultipart(processedRequest);
        }
    }
}
```

**作用：**
- 清理文件上传的临时文件
- 处理异步请求的后续操作
- 确保资源正确释放

---

## 完整的请求处理流程图

```
请求到达
    ↓
检查是否文件上传请求 (checkMultipart)
    ↓
根据请求找到 Handler (getHandler)
    ↓
找到对应的 HandlerAdapter (getHandlerAdapter)
    ↓
处理 Last-Modified 缓存
    ↓
执行拦截器 preHandle (applyPreHandle)
    ↓
调用 Controller 方法 (handle)
    ↓
检查是否异步处理
    ↓
应用默认视图名称 (applyDefaultViewName)
    ↓
执行拦截器 postHandle (applyPostHandle)
    ↓
处理执行结果 (processDispatchResult)
    ├── 处理异常 (processHandlerException)
    └── 渲染视图 (render)
    ↓
执行拦截器 afterCompletion (triggerAfterCompletion)
    ↓
清理资源 (finally)
    ↓
响应返回
```

## 关键组件说明

### HandlerExecutionChain
- 包含 Handler 和拦截器链
- 负责管理拦截器的执行

### HandlerMapping
- 根据请求找到对应的 Handler
- 常见实现：RequestMappingHandlerMapping

### HandlerAdapter
- 适配不同类型的 Handler
- 统一调用接口
- 常见实现：RequestMappingHandlerAdapter

### HandlerInterceptor
- 拦截器接口
- 三个方法：preHandle、postHandle、afterCompletion

### ModelAndView
- 包含模型数据和视图信息
- 传递给视图渲染器

### ViewResolver
- 解析视图名称，返回 View 对象
- 常见实现：InternalResourceViewResolver、ThymeleafViewResolver

### HandlerExceptionResolver
- 处理请求处理过程中的异常
- 常见实现：ExceptionHandlerExceptionResolver

## 实战示例

### 示例1：完整的请求处理流程

```java
// Controller
@Controller
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public ModelAndView getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        ModelAndView mv = new ModelAndView("userDetail");
        mv.addObject("user", user);
        return mv;
    }
}

// 拦截器
@Component
public class LogInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        System.out.println("preHandle: " + request.getRequestURI());
        return true; // 继续执行
    }
    
    @Override
    public void postHandle(HttpServletRequest request, 
                          HttpServletResponse response, 
                          Object handler, 
                          ModelAndView modelAndView) throws Exception {
        System.out.println("postHandle: " + modelAndView.getViewName());
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler, 
                               Exception ex) throws Exception {
        System.out.println("afterCompletion: 请求完成");
    }
}

// 配置拦截器
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private LogInterceptor logInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/static/**");
    }
}
```

**执行顺序：**
```
1. checkMultipart: 检查是否文件上传
2. getHandler: 找到 UserController.getUser 方法
3. getHandlerAdapter: 找到 RequestMappingHandlerAdapter
4. LogInterceptor.preHandle: 执行拦截器前置处理
5. UserController.getUser: 执行业务逻辑
6. LogInterceptor.postHandle: 执行拦截器后置处理
7. processDispatchResult: 渲染 userDetail 视图
8. LogInterceptor.afterCompletion: 完成处理
```

### 示例2：异常处理

```java
@Controller
@RequestMapping("/user")
public class UserController {
    
    @GetMapping("/{id}")
    public ModelAndView getUser(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("用户ID无效");
        }
        User user = userService.getUser(id);
        if (user == null) {
            throw new UserNotFoundException("用户不存在: " + id);
        }
        ModelAndView mv = new ModelAndView("userDetail");
        mv.addObject("user", user);
        return mv;
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleUserNotFound(UserNotFoundException ex) {
        ModelAndView mv = new ModelAndView("error/404");
        mv.addObject("message", ex.getMessage());
        return mv;
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ModelAndView handleIllegalArgument(IllegalArgumentException ex) {
        ModelAndView mv = new ModelAndView("error/400");
        mv.addObject("message", ex.getMessage());
        return mv;
    }
}
```

**异常处理流程：**
```
1. Controller 方法抛出异常
2. 跳过 postHandle（不执行）
3. processDispatchResult 捕获异常
4. processHandlerException 处理异常
5. ExceptionHandlerExceptionResolver 找到 @ExceptionHandler 方法
6. 执行异常处理方法，返回 ModelAndView
7. 渲染错误视图
8. 执行 afterCompletion（即使有异常也会执行）
```

### 示例3：异步请求处理

```java
@Controller
@RequestMapping("/async")
public class AsyncController {
    
    @GetMapping("/callable")
    public Callable<ModelAndView> callableDemo() {
        System.out.println("主线程: " + Thread.currentThread().getName());
        
        return () -> {
            System.out.println("异步线程: " + Thread.currentThread().getName());
            Thread.sleep(2000); // 模拟耗时操作
            
            ModelAndView mv = new ModelAndView("result");
            mv.addObject("message", "异步处理完成");
            return mv;
        };
    }
    
    @GetMapping("/deferred")
    public DeferredResult<ModelAndView> deferredDemo() {
        DeferredResult<ModelAndView> result = new DeferredResult<>(5000L);
        
        // 异步处理
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(2000);
                ModelAndView mv = new ModelAndView("result");
                mv.addObject("message", "延迟结果返回");
                result.setResult(mv);
            } catch (Exception e) {
                result.setErrorResult(e);
            }
        });
        
        return result;
    }
}
```

**异步处理流程：**
```
1. doDispatch 开始处理请求
2. 执行 preHandle
3. handle 方法返回 Callable/DeferredResult
4. asyncManager.isConcurrentHandlingStarted() 返回 true
5. doDispatch 直接返回，不执行 postHandle
6. 请求线程释放，可以处理其他请求
7. 异步任务在其他线程中执行
8. 异步任务完成后，重新进入 doDispatch
9. 渲染视图，返回响应
```

## 常见问题

### Q1: 拦截器的三个方法执行顺序是什么？

**A:** 
- `preHandle`：按配置顺序执行，在 Controller 方法之前
- `postHandle`：按配置逆序执行，在 Controller 方法之后
- `afterCompletion`：按配置逆序执行，在视图渲染之后

**示例：**
```
拦截器1.preHandle
拦截器2.preHandle
拦截器3.preHandle
    Controller 方法执行
拦截器3.postHandle
拦截器2.postHandle
拦截器1.postHandle
    视图渲染
拦截器3.afterCompletion
拦截器2.afterCompletion
拦截器1.afterCompletion
```

### Q2: 如果 preHandle 返回 false 会怎样？

**A:** 
- 立即停止请求处理
- 不再执行后续拦截器和 Controller 方法
- 已执行的拦截器会执行 `afterCompletion`

### Q3: postHandle 和 afterCompletion 的区别？

**A:** 
- `postHandle`：
  - 在 Controller 方法执行后，视图渲染前执行
  - 可以修改 ModelAndView
  - 如果 Controller 抛异常，不会执行
  
- `afterCompletion`：
  - 在视图渲染后执行
  - 无论是否有异常都会执行
  - 用于资源清理

### Q4: HandlerAdapter 的作用是什么？

**A:** 
- 适配器模式的应用
- 统一不同类型 Handler 的调用方式
- 解耦 DispatcherServlet 和具体的 Handler 实现
- 支持扩展新的 Handler 类型

### Q5: 如何自定义异常处理？

**A:** 
有三种方式：
1. `@ExceptionHandler`：在 Controller 中处理
2. `@ControllerAdvice`：全局异常处理
3. 实现 `HandlerExceptionResolver` 接口

```java
// 方式1：Controller 级别
@Controller
public class UserController {
    @ExceptionHandler(UserNotFoundException.class)
    public ModelAndView handleException(UserNotFoundException ex) {
        // 处理异常
    }
}

// 方式2：全局级别
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(Exception ex) {
        // 处理异常
    }
}

// 方式3：自定义解析器
@Component
public class CustomExceptionResolver implements HandlerExceptionResolver {
    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Object handler,
                                        Exception ex) {
        // 处理异常
    }
}
```

## 总结

`doDispatch` 方法是 Spring MVC 的核心，它协调了整个请求处理流程：

1. **请求预处理**：检查文件上传、查找 Handler
2. **拦截器前置处理**：权限验证、日志记录
3. **调用 Controller**：执行业务逻辑
4. **拦截器后置处理**：修改 ModelAndView
5. **视图渲染**：生成响应内容
6. **完成处理**：清理资源

这个设计体现了以下设计模式和原则：
- **适配器模式**：HandlerAdapter
- **策略模式**：HandlerMapping、ViewResolver
- **责任链模式**：HandlerInterceptor
- **模板方法模式**：整个 doDispatch 流程
- **单一职责原则**：每个组件职责明确

理解 `doDispatch` 方法对于掌握 Spring MVC 框架至关重要，它是连接所有组件的中枢。

