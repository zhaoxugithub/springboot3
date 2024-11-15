package com.serendipity.mapper;

import com.serendipity.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> getUsers();

    void insertBatch(List<User> users);

    void insert(User user);

    void deleteById(Integer integer);
}
