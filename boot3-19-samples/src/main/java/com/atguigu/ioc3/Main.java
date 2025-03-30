package com.atguigu.ioc3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

/*
    bean的初始化顺序：
    静态变量或者静态语句块 -> 实例变量或者初始化语句块 -> 构造方法 -> @Autowired
 */

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        var ioc = SpringApplication.run(Main.class, args);
        String[] beanDefinitionNames = ioc.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).forEach(System.out::println);
    }
}
