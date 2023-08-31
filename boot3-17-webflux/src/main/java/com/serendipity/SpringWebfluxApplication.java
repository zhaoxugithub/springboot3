package com.serendipity;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@ServletComponentScan("com.javaming.study.webflux.servlet")
// 设置开启mongodb响应式存储
@EnableReactiveMongoRepositories
public class SpringWebfluxApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringWebfluxApplication.class, args);
    }
}
