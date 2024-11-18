package com.serendipity.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.serendipity.entity.User;
import com.serendipity.mapper.UserMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


@Service
public class UserService implements UserServiceInter {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AsyncService asyncService;

    private static final List<Integer> ids = Lists.newArrayList();

    @Override
    public List<User> getUsers() {
        return userMapper.getUsers();
    }

    @Override
    public List<User> getUser1() {
        // 多线程查询
        List<Future<?>> futures = Lists.newArrayList();
        int BatchSize = 10000;
        ExecutorService executorService1 = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            int finalI = i;
            Future<?> submit = executorService1.submit(() -> {
                List<User> usersByLimit = userMapper.getUsersByLimit(10000, finalI * BatchSize);
            });

            futures.add(submit);
        }
        return null;
    }


    @Override
    public void addUsers(List<User> users) {
        userMapper.insertBatch(users);
    }

    @Override
    public String exportData() {
        long start = System.currentTimeMillis();
        System.out.println("start export data:" + start);
        InputStream dataInputStream = getDataInputStream();
        System.out.println("end export data:" + (System.currentTimeMillis() - start));
        String fileName = "user_data" + start + ".xlsx";
        asyncService.uploadOSS2(fileName, dataInputStream);
        return fileName;
    }

    @Override
    public List<User> getUsersPartition() {
        return List.of();
    }

    private InputStream getDataInputStream() {
        List<User> users = getUsers();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, User.class);
        writerBuilder.sheet("User")
                .doWrite(users);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
