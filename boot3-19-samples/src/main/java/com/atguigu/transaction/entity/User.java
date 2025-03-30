package com.atguigu.transaction.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long userId;
    private String username;
    private String email;
    private String passwordHash;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private UserProfile profile; // 关联对象
}
