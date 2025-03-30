package com.atguigu.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailLister {

    // 监听用户注册事件(异步)
    /*
       1. 如果是使用同步发送: 发送方和接收方 使用同一个线程
       2. 如果是使用异步发送: 发送方和接收方 使用不同的线程;发送方发送之后,立即返回，相应速度很快
     */
    @Async
    @EventListener
    public void sendWelcomeEmail(UserRegisterEvent event) {
        log.info("[邮件服务] 线程: {}", Thread.currentThread().getName());
        log.info("正在发送欢迎邮件至: {}", event.getEmail());
        // 模拟邮件发送耗时
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ignored) {
        }
        log.info("邮件发送完成!");
    }
}
