package com.atguigu.ioc4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class BeanLifecycleDemo {

    public static void main(String[] args) {
        var ioc = SpringApplication.run(BeanLifecycleDemo.class, args);
        String[] beanDefinitionNames = ioc.getBeanDefinitionNames();
        Arrays.stream(beanDefinitionNames).forEach(System.out::println);
        ExampleBean bean = ioc.getBean(ExampleBean.class);
        ioc.close(); // 触发销毁
    }
}
