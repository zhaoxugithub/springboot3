package com.serendipity.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private String name;
    private int age;
    private double height;
    private String gender;
    private String address;
    private Date createdTime;
    private Date updatedTime;
}
