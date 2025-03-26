package com.atguigu.aop1;


import com.atguigu.aop1.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
https://www.yuque.com/hollis666/npdikg/nget4r5wl2imegi7
一个订单的创建，可能需要以下步骤
1.权限校验
2.事务管理
3.创建订单
4.日志打印
如果使用AOP思想，我们就可以把这四步当成四个“切面”，让业务人员专注开发第三个切面，其他三个切面则是基础的通用逻辑，统一交给AOP封装和管理。
 */
@SpringBootApplication
public class Main implements CommandLineRunner {

    @Autowired
    private OrderService orderService;

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            orderService.createOrder();
            System.out.println("=====================================");
            orderService.updateOrder(null);
        } catch (Exception e) {
            System.err.println("发生异常: " + e.getMessage());
        }
    }
}