package com.atguigu.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogListener {

    @Async
    @EventListener
    public void logRegistration(UserRegisterEvent event) {
        log.info("[日志服务] 线程: {}", Thread.currentThread().getName());
        log.info("记录用户注册日志: {}", event.getUsername());
    }
}
