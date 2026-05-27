package com.atguigu.transaction.mapper;

import com.atguigu.transaction.entity.UserProfile;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {

    void updateUserProfile(Long userId, String phone);

    void insertAll(List<UserProfile> list);

    void deleteAll();
}
