package com.atguigu.ioc3;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class ServiceTest2 {

    private String name;
    private static final String staticName1 = "staticName";

    // 相互依赖的,导致循环依赖，添加lazy注解解决
    @Lazy
    @Resource
    private ServiceTest1 serviceTest1;

    static {
        System.out.println("serviceTest2 static block");
    }

    {
        // serviceTest1.init(); 会导致NPE
        System.out.println("serviceTest2 instance block");
    }

    public ServiceTest2() {
        // 会导致NPE
        // serviceTest1.init();
        System.out.println("serviceTest2 constructor");
    }
}
