package com.atguigu.boot.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author lfy
 * @Description
 * @create 2023-03-29 12:22
 */
@Data
@ConfigurationProperties(prefix = "sheep")
public class Sheep {
    private Long id;
    private String name;
    private Integer age;

    @Override
    public String toString() {
        return "Sheep{" + "id=" + id + ", name='" + name + '\'' + ", age=" + age + '}';
    }
}
