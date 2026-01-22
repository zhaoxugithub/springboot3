package com.atguigu.rocketmq;

import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @Resource
    private ProducerService producerService;

    @PostMapping("/send")
    public String send(@RequestParam(defaultValue = "my-tag") String tag, @RequestParam String msg) {
        producerService.send(tag, msg);
        return "sent";
    }
}

