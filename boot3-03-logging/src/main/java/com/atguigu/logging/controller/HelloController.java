package com.atguigu.logging.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lfy
 * @Descriptio}
 * @create 2023-03-31 14:03
 */
@Slf4j
@RestController
public class HelloController {
    @GetMapping("/h/**")
    public String hello(String a, String b) {
        for (int i = 0; i < 1000; i++) {
            log.trace(Thread.currentThread()
                    .getName() + ":trace 日志.....");
            log.debug(Thread.currentThread()
                    .getName() + ":debug 日志.....");
            // SpringBoot底层默认的日志级别 info
            log.info(Thread.currentThread()
                    .getName() + ":info 日志..... 参数a:{} b:{}", a, b);
            log.warn(Thread.currentThread()
                    .getName() + ":warn 日志...");
            log.error(Thread.currentThread()
                    .getName() + ":error 日志...");
            log.info(Thread.currentThread()
                    .getName() + ":这个是一次请求");
        }
        return "hello";
    }

    //
    @GetMapping("/log1")
    public void log1() {
        log.info("info 日志.....");
        String userId = "1001";
        String operation = "del";
        long duration = 35;
        // 推荐：使用占位符
        log.info("用户 {} 执行操作 {}, 耗时 {}ms", userId, operation, duration);
        // 不推荐：字符串拼接
        log.info("用户 " + userId + " 执行操作 " + operation + ", 耗时 " + duration + "ms");
    }


    // 选择合理的日志界别
    @GetMapping("/log2")
    public String test02() {

        String a = "a";
        String b = "b";
        String threadName = Thread.currentThread().getName();
        log.debug("{}: 开始处理请求, 参数 a={}, b={}", threadName, a, b);
        try {
            // 业务逻辑
            log.info("{}: 请求处理完成", threadName);
            return "hello";
        } catch (Exception e) {
            log.error("{}: 请求处理失败", threadName, e);
            throw e;
        }
    }

    // 避免在循环中大量打印日志
    @GetMapping("/log3")
    public String hello(){

        String a = "a";
        String b = "b";
        String threadName = Thread.currentThread().getName();
        log.info("{}: 开始处理请求, 参数 a={}, b={}", threadName, a, b);

        // 避免在循环中频繁打日志
        for (int i = 0; i < 1000; i++) {
            // 只在必要时打日志，或者降低频率
            if (i % 100 == 0) {
                log.debug("{}: 处理进度 {}/1000", threadName, i);
            }
        }
        log.info("{}: 请求处理完成", threadName);
        return "hello";
    }
}
