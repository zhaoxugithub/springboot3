package com.serendipity.servlet;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "SSE", urlPatterns = "/SSE")
public class SSE extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("utf-8");
        for (int i = 0; i < 5; i++) {
            // 指定事件标识
            response.getWriter()
                    .write("event:me\n");
            // 格式：data: + 数据 + 2个回车
            response.getWriter()
                    .write("data:" + i + "\n\n");
            response.getWriter()
                    .flush();
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}