package com.serendipity.servlet;


import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "AsyncServlet", urlPatterns = "/AsyncServlet", asyncSupported = true)
public class AsyncServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        long t1 = System.currentTimeMillis();
        // 1.开启异步
        AsyncContext asyncContext = request.startAsync();
        // 2.把我们要执行的代码放到一个独立的线程中，多线程/线程池
        CompletableFuture.runAsync(() ->
                // 执行业务代码
        {
            try {
                doSomeTing(asyncContext, asyncContext.getRequest(), asyncContext.getResponse());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        System.out.println("async use:" + (System.currentTimeMillis() - t1));
    }

    private void doSomeTing(AsyncContext asyncContext, ServletRequest request, ServletResponse response) throws IOException {
        // 模拟耗时操作
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        response.getWriter().append("async done");
        // 3.业务代码处理完毕，通知结束
        asyncContext.complete();
    }
}
