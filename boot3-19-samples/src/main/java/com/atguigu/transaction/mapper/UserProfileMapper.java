package com.atguigu.transaction.mapper;

import com.atguigu.transaction.entity.UserProfile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    void updateUserProfile(Long userId, String phone);
}
