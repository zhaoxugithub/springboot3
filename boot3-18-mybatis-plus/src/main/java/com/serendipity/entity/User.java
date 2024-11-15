package com.serendipity.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.util.Date;

@Data
public class User {
    @ExcelProperty(value = "姓名")
    private String name;
    @ExcelProperty(value = "年龄")
    private int age;
    @ExcelProperty(value = "身高")
    private double height;
    @ExcelProperty(value = "性别")
    private String gender;
    @ExcelProperty(value = "地址")
    private String address;
}
