package com.atguigu.boot.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ClassName Teacher
 * Description TODO
 * Author 11931
 * Date 2023-07-11:23:47
 * Version 1.0
 **/
@Data
@ConfigurationProperties(prefix = "tea")
public class Teacher {
    private String name;
    private String age;
    private String type;

    @Override
    public String toString() {
        return "Teacher{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
