package com.atguigu.boot.bean;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * @author lfy
 * @Description
 * @create 2023-03-28 17:00
 */
//@Component
@Data
public class Cat {
    private Long id;
    private String name;

    @Override
    public String toString() {
        return "Cat{" + "id=" + id + ", name='" + name + '\'' + '}';
    }
}
