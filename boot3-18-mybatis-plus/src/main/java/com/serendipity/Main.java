package com.serendipity;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


@EnableAsync
@MapperScan(basePackages = "com.serendipity.mapper")
@SpringBootApplication
public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        SpringApplication.run(Main.class, args);
    }
}