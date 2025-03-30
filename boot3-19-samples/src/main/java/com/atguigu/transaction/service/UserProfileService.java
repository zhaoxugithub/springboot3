package com.atguigu.transaction.service;

import com.atguigu.transaction.entity.UserProfile;
import com.atguigu.transaction.mapper.UserProfileMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService extends ServiceImpl<UserProfileMapper, UserProfile> implements IService<UserProfile> {

    @Resource
    private UserProfileMapper userProfileMapper;

    public void updateUserP(Long id) {
        UserProfile userProfile = new UserProfile();
        userProfile.setUserId(id);
        userProfile.setPhone("123");
        userProfileMapper.updateUserProfile(userProfile.getUserId(), userProfile.getPhone());
    }
}
