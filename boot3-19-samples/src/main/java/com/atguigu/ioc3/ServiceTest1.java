package com.atguigu.ioc3;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class ServiceTest1 {

    private String name;
    private static final String staticName1 = "staticName";

    @Resource
    private ServiceTest2 serviceTest2;

    static {
        System.out.println("serviceTest1 static block");
    }

    public ServiceTest1() {
        System.out.println("serviceTest1 constructor");
    }

    public void init() {
        System.out.println("service Test1 init");
    }
}
