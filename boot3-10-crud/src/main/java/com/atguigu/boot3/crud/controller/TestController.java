package com.atguigu.boot3.crud.controller;

import com.atguigu.boot3.starter.robot.annotation.EnableRobot;
import com.atguigu.boot3.starter.robot.controller.RobotController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ClassName TestController
 * Description TODO
 * Author 11931
 * Date 2023-07-23:0:22
 * Version 1.0
 **/
@EnableRobot
@RestController
public class TestController {

    private Integer count = 0;

    @Autowired
    private RobotController robotController;

    @GetMapping("/ss")
    public void test() {
        String s = robotController.sayHello();
        System.out.println(s);
    }

    @GetMapping("/per")
    public Integer testPerformance() {
        for (int i = 0; i < 1000000000; i++) {
            count = count + 1;
        }
        return count;
    }
}
