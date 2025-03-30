package com.atguigu.transaction.service;

import com.atguigu.transaction.entity.User;
import com.atguigu.transaction.mapper.UserMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IService<User> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private TransactionTemplate transactionTemplate;

    public List<User> query1() {
        return list();
    }

    // 申明式事务
    // @Transactional

    // 指定遇到什么异常的时候进行回滚
    @Transactional(rollbackFor = Exception.class)
    public void updateUser1() {
        updateUserInfo(1L);
        userProfileService.updateUserP(1L);
    }

    // 编程式事务
    public void updateUser2() {
        transactionTemplate.execute(status -> {
            updateUserInfo(1L);
            userProfileService.updateUserP(1L);
            return true;
        });
    }

    // 异常事务正常回滚
    @Transactional
    public void updateUser3() {
        updateUserInfo(2L);
        int i = 1 / 0;
        userProfileService.updateUserP(2L);
    }

    // 异常事务正常回滚
    public void updateUser4() {
        transactionTemplate.execute(status -> {
            updateUserInfo(1L);
            int i = 1 / 0;
            userProfileService.updateUserP(1L);
            return true;
        });
    }


    // 事务失效
    @Transactional
    void updateUser05() {
        try {
            updateUserInfo(1L);
            int i = 1 / 0;
            userProfileService.updateUserP(1L);
        } catch (Exception e) {
            // 处理异常
        }
    }


    private void updateUserInfo(Long id) {
        User user = new User();
        user.setUserId(id);
        user.setUsername("test1");
        userMapper.updateUser(user.getUserId(), user.getUsername());
    }
}
