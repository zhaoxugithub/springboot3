package com.atguigu.transaction.service;

import com.atguigu.transaction.entity.User;
import com.atguigu.transaction.entity.UserProfile;
import com.atguigu.transaction.mapper.UserMapper;
import com.atguigu.transaction.mapper.UserProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;



@Service
public class UserService extends ServiceImpl<UserMapper, User> implements IService<User> {

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

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
    public void updateUser05() {
        try {
            updateUserInfo(1L);
            int i = 1 / 0;
            userProfileService.updateUserP(1L);
        } catch (Exception e) {
        }
    }


    private void updateUserInfo(Long id) {
        User user = new User();
        user.setUserId(id);
        user.setUsername("test1");
        userMapper.updateUser(user.getUserId(), user.getUsername());
    }

    @Transactional
    public void refresh() throws ParseException {

        userMapper.deleteAll();
        userMapper.insertAll(buildUserList());

        userProfileMapper.deleteAll();
        userProfileMapper.insertAll(buildUserProfile());
    }

    private List<UserProfile> buildUserProfile() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = "2025-05-20 11:52:49";
        UserProfile userProfile = new UserProfile(1L, 1L, "zhangsan", "1",
                sdf.parse(timeStr), "123456789"
        );
        UserProfile userProfile2 = new UserProfile(2L, 2L, "lisi", "1",
                sdf.parse(timeStr), "123456789"
        );
        UserProfile userProfile3 = new UserProfile(3L, 3L, "wangwu", "1",
                sdf.parse(timeStr), "123456789"
        );
        List<UserProfile> userProfileList = new ArrayList<>();
        userProfileList.add(userProfile);
        userProfileList.add(userProfile2);
        userProfileList.add(userProfile3);
        return userProfileList;
    }

    private List<User> buildUserList() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStr = "2025-05-20 11:52:49";
        // 第一个对象
        User user0 = new User(1L, "zhangsan", "zhangsan@atguigu.com",
                "123455abc", "1", sdf.parse(timeStr), sdf.parse(timeStr), null);
        // 第二个对象
        User user1 = new User(2L, "lisi", "lisi@atguigu.com",
                "654320def", "1", sdf.parse(timeStr), sdf.parse(timeStr), null);
        // 第三个对象
        User user2 = new User(3L, "wangwu", "wangwu@atguigu.com",
                "abcdef122", "0", sdf.parse(timeStr), sdf.parse(timeStr), null);
        // 放入集合
        List<User> userList = new ArrayList<>();
        userList.add(user0);
        userList.add(user1);
        userList.add(user2);
        return userList;
    }
}
