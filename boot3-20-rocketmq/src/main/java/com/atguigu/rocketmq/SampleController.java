package com.atguigu.rocketmq;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SampleController {

    @Autowired
    private ProducerService producerService;

//    public SampleController(ProducerService producerService) {
//        this.producerService = producerService;
//    }

    @PostMapping("/send")
    public String send(@RequestParam(defaultValue = "my-tag") String tag,
                       @RequestParam String msg) {
        producerService.send(tag, msg);
        return "sent";
    }
}

