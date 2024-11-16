package com.serendipity.utils;

import com.alibaba.excel.EasyExcel;
import com.serendipity.entity.User;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CreateExcelUtils {


    public static void main(String[] args) throws FileNotFoundException, ExecutionException, InterruptedException {
        createMoreThread();
    }

    public static void createMoreThread() throws FileNotFoundException, ExecutionException, InterruptedException {
        // String fileName = "D:\\user.xlsx";
        String fileName = "/Users/zhaoxu/Documents/IdeaProjects/springboot3/boot3-18-mybatis-plus/src/main/java/com/serendipity/user.xlsx";
        OutputStream outputStream = new FileOutputStream(fileName);

        // Create a thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        // Create tasks for each thread
        List<Future<List<User>>> futures = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int start = i * 250000;
            int end = (i + 1) * 250000;
            futures.add(executorService.submit(() -> mockData(start, end)));
        }
        // Collect results from all threads
        List<User> allUsers = new ArrayList<>();
        for (Future<List<User>> future : futures) {
            allUsers.addAll(future.get());
        }

        // Write data to Excel
        EasyExcel.write(outputStream, User.class)
                 .head(getHeaderReport())
                 .sheet("用户信息")
                 .doWrite(allUsers);

        // Shutdown the executor service
        executorService.shutdown();
    }

    public static List<User> mockData(int start, int end) {
        List<User> userList = new ArrayList<>();
        for (int i = start; i < end; i++) {
            User user = new User();
            user.setName("张三" + i);
            user.setAge(i);
            user.setAddress("北京市海淀区" + i);
            user.setGender("男");
            user.setHeight(170 + i);

            userList.add(user);
        }
        return userList;
    }


    public static void createSingleExcel() throws IOException {
        String fileName = "D:\\user.xlsx";
        OutputStream outputStream = new FileOutputStream(fileName);
        EasyExcel.write(outputStream, User.class)
                 .head(getHeaderReport())
                 .sheet("用户信息")
                 .doWrite(mockData("线程1"));

        outputStream.close();
    }

    private static List<List<String>> getHeaderReport() {
        List<List<String>> list = new ArrayList<>();
        List<String> head0 = new ArrayList<>();
        head0.add("姓名");
        List<String> head1 = new ArrayList<>();
        head1.add("年龄");
        List<String> head2 = new ArrayList<>();
        head2.add("身高");
        List<String> head3 = new ArrayList<>();
        head3.add("性别");
        List<String> head4 = new ArrayList<>();
        head4.add("地址");
        list.add(head0);
        list.add(head1);
        list.add(head2);
        list.add(head3);
        list.add(head4);
        return list;
    }


    public static List<User> mockData(String name) {
        System.out.println("线程：" + name + "开始执行");
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 10000_00; i++) {
            User user = new User();
            user.setName("张三" + i);
            user.setAge(i);
            user.setAddress("北京市海淀区" + i);
            user.setGender("男");
            user.setHeight(170 + i);

            userList.add(user);
        }
        return userList;
    }
}
