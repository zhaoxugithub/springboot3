package com.atguigu.boot.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Autowired
    private OrderService orderService;

    @RequestMapping("/v1")
    public void process1() {
        orderService.process();
    }

    @RequestMapping("/v2")
    private void process2() {
        orderService.process2();
    }

    @RequestMapping("/v3")
    private void process3() {
        orderService.process3();
    }

    @RequestMapping("/v4")
    private void process4() {
        orderService.process4();
    }
}
