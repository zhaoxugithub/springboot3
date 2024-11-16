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

    private InputStream getDataInputStream() {
        List<User> users = getUsers();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ExcelWriterBuilder writerBuilder = EasyExcel.write(outputStream, User.class);
        writerBuilder.sheet("User")
                     .doWrite(users);

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}
