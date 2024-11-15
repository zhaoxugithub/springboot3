package com.serendipity.service;

import com.serendipity.entity.User;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@EnableAsync
@Service
public interface UserServiceInter {

    List<User> getUsers();

    void addUsers(List<User> users);

    // 异步导出
    String exportData() throws InterruptedException;
}
