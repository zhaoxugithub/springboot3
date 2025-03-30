package com.atguigu.event;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {
    @Resource
    private UserService userService;

    @PostMapping
    public String registerUser() {
        userService.register("张三", "zhangsan@example.com");
        return "注册请求已受理";
    }
}