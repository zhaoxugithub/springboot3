package com.atguigu.boot.bean;

import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author lfy
 * @Description
 * @create 2023-03-30 9:26
 */
@Data
public class Child {
    private String name;
    private Integer age;
    private Date birthDay;
    /**
     * 数组
     */
    private List<String> text;
}
