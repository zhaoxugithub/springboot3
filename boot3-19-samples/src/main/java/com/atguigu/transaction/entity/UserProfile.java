package com.atguigu.transaction.entity;

import lombok.Data;

import java.util.Date;

@Data
public class UserProfile {
    private Long profileId;
    private Long userId;
    private String fullName;
    private String gender;
    private Date birthdate;
    private String phone;
}
