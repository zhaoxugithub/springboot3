package com.atguigu.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 用户注册事件
 * 1. 继承 ApplicationEvent
 * 2. 定义事件源
 * 3. 定义事件数据
 */
@Getter
public class UserRegisterEvent extends ApplicationEvent {
    private final String username;
    private final String email;

    public UserRegisterEvent(Object source, String username, String email) {
        super(source);
        this.username = username;
        this.email = email;
    }
}
