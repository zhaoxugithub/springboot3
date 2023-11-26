package com.atguigu.boot.bean;

import lombok.Data;

/**
 * @author lfy
 * @Description
 * @create 2023-03-29 12:16
 */

//@Component
@Data
public class Pig {
    private Long id;
    private String name;
    private Integer age;

    @Override
    public String toString() {
        return "Pig{" + "id=" + id + ", name='" + name + '\'' + ", age=" + age + '}';
    }
}
