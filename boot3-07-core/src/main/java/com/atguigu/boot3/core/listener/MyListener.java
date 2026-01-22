package com.atguigu.boot3.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * @author lfy
 * @Description
 * @create 2023-04-24 16:35
 */
@Slf4j
public class MyListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        log.warn("[MyListener] 线程: {}, event:{}", Thread.currentThread().getName(), event.toString());
    }
}
