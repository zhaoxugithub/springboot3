package com.atguigu.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BonusListener {
    @Async
    @EventListener
    public void grantSignupBonus(UserRegisterEvent event) {
        log.info("[积分服务] 线程: {}", Thread.currentThread().getName());
        log.info("为用户 {} 赠送100积分", event.getUsername());
    }
}