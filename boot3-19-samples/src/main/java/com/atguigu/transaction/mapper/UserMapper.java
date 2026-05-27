package com.atguigu.transaction.mapper;

import com.atguigu.transaction.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    void updateUser(Long userId, String username);
    void insertAll(List<User> list);
    void  deleteAll();
}
