package com.serendipity.controller;

import com.alibaba.excel.EasyExcel;
import com.serendipity.entity.User;
import com.serendipity.service.MyDataModelListener;
import com.serendipity.service.UserServiceInter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserServiceInter userService;

    @GetMapping("/list")
    public List<User> getUsers() {
        long start = System.currentTimeMillis();
        System.out.println("start export data:" + start);
        List<User> users = userService.getUsers();
        System.out.println("end export data:" + (System.currentTimeMillis() - start));
        return users;
    }

    @PostMapping("/put")
    public void upload(MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();
        EasyExcel.read(inputStream, User.class, new MyDataModelListener(userService))
                 .sheet()
                 .doRead();
    }


    @GetMapping("/download")
    public void download(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8")
                                    .replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        long start = System.currentTimeMillis();
        System.out.println("start export data:" + start);
        List<User> users = userService.getUsers();
        System.out.println("end export data:" + (System.currentTimeMillis() - start));

        EasyExcel.write(response.getOutputStream(), User.class)
                 .sheet("用户信息")
                 .doWrite(users);
    }


    @GetMapping("/downLoadSync")
    public String downLoadSync() throws InterruptedException {
        System.out.println("t1:" + Thread.currentThread()
                                         .getName());
        return userService.exportData();
    }
}
