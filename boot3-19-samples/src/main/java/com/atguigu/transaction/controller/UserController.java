package com.atguigu.transaction.controller;

import com.atguigu.transaction.entity.User;
import com.atguigu.transaction.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
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

    @GetMapping("/update/{version}")
    public void updateUser(@PathVariable Long version) throws ParseException {
        if (version == 1) {
            userService.updateUser1();
        } else if (version == 2) {
            userService.updateUser2();
        } else if (version == 3) {
            userService.updateUser3();
        } else if (version == 4) {
            userService.updateUser4();
        } else if (version == 5) {
            userService.updateUser05();
        } else {
            userService.refresh();
        }
    }

    @GetMapping("/refresh")
    public void refresh() throws ParseException {
        userService.refresh();
    }

    @GetMapping("/update2")
    public void updateUser2() {
        userService.updateUser2();
    }

}
