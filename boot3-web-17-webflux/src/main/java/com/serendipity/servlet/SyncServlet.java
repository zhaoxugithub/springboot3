package com.serendipity.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author : mpy
 * @date : 2021/11/3 3:27 下午
 * @description:
 */
@WebServlet(name = "SyncServlet", urlPatterns = "/SyncServlet")
public class SyncServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long t1 = System.currentTimeMillis();
        // 执行业务代码
        doSomeTing(request, response);
        System.out.println("sync use:" + (System.currentTimeMillis() - t1));
    }

    private void doSomeTing(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 模拟耗时操作
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response.getWriter().append("done");
    }
}
