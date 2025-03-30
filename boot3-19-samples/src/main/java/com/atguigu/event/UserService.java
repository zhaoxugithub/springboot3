package com.atguigu.event;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * 用户注册服务 发布
 * 1. 注册用户
 * 2. 发布用户注册事件
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private ApplicationEventPublisher eventPublisher;

    public void register(String username, String email) {
        log.info("[注册服务] 开始注册用户: {}", username);
        // 模拟数据库操作
        log.info("[注册服务] 用户注册成功!");
        eventPublisher.publishEvent(new UserRegisterEvent(this, username, email));
    }
}
