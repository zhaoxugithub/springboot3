package com.atguigu.transaction.controller;

import com.atguigu.transaction.entity.User;
import com.atguigu.transaction.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/query")
    public List<User> query() {
        return userService.query1();
    }

    @GetMapping("/update")
    public void updateUser() {
        userService.updateUser1();
    }

    @GetMapping("/update2")
    public void updateUser2() {
        userService.updateUser2();
    }

}
