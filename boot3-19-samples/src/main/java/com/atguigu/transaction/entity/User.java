package com.atguigu.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
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
