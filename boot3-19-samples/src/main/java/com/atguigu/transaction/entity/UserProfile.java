package com.atguigu.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UserProfile {
    private Long profileId;
    private Long userId;
    private String fullName;
    private String gender;
    private Date birthdate;
    private String phone;
}
