package com.atguigu.boot.bean;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @author lfy
 * @Description
 * @create 2023-03-28 17:00
 */
//@Component
@Data
public class Dog {
    private Long id;
    private String name;

    @Override
    public String toString() {
        return "Dog{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}