package com.atguigu.boot.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * ClassName Student
 * Description TODO
 * Author 11931
 * Date 2023-07-26:17:57
 * Version 1.0
 **/

@Data
@ConfigurationProperties(prefix = "student")
public class Student {
    private String name;
    private String age;
    private Integer wight;

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", age='" + age + '\'' +
                ", wight=" + wight +
                '}';
    }
}
