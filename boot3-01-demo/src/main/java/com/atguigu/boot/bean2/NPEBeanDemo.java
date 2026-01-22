package com.atguigu.boot.bean2;

import com.atguigu.boot.bean.Person;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NPEBeanDemo {
    // @Autowired
    @Resource
    private Person person;
    // private final String name;

    // 静态变量或静态语句块 ->实例变量或初始化语句块 ->构造方法 ->@Autowired
    public NPEBeanDemo() {
        // 这里可能会NPE
        // this.name = person.getName();
    }
}
