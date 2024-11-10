package com.serendipity.service;

import com.serendipity.entity.User;
import com.serendipity.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Resource
    private UserMapper userMapper;


    public List<User> getUsers() {
       return userMapper.getUsers();
    }


}
