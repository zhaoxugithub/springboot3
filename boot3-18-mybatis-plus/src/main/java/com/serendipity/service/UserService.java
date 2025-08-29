package com.serendipity.service;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.serendipity.entity.User;
import com.serendipity.mapper.UserMapper;
import org.apache.commons.compress.utils.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.*;


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

        int batchSize = 20000;
        int total = userMapper.selectCount();

        int batch = total / batchSize;
        ExecutorService executorService = Executors.newCachedThreadPool();

        CopyOnWriteArrayList<User> result = new CopyOnWriteArrayList<>();

        List<Future<?>> list = Lists.newArrayList();
        for (int i = 0; i < batch; i++) {
            int finalI = i;
            Future<?> submit = executorService.submit(() -> {
                result.addAll(userMapper.getUsersByLimit(batchSize, finalI * batchSize));
            });
            list.add(submit);
        }

        list.forEach(future -> {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        executorService.shutdown();

        System.out.println("result size: " + result.size());
        return result;
    }


    @Override
    public void addUsers(List<User> users) {
        userMapper.insertBatch(users);
    }

    @Override
    public String exportData() {
        InputStream dataInputStream = getDataInputStream();
        String fileName = "user_data" + System.currentTimeMillis() + ".xlsx";
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
