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
}
