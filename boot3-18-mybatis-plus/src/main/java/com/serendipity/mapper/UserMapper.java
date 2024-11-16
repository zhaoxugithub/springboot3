package com.serendipity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.serendipity.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<User> getUsers();

    void insertBatch(List<User> users);
}
