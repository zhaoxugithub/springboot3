package com.atguigu.boot.config;

import com.atguigu.boot.bean2.ComputerOp;
import com.atguigu.boot.bean2.Phone;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/*
  注入+属性绑定用法：
   方法一：
    实体类：@Data
    配置类:
        类上：加@Configuration
        方法上： new 实体类对象方法, 方法上加@Bean 和 @ConfigurationProperties(prefix = "实体类对象属性")
   方法二：
      实体类：@Data 和 ConfigurationProperties
      配置类:
        类上：加@Configuration 和 EnableConfigurationProperties
*/

@Configuration
@EnableConfigurationProperties(Phone.class)
public class ComputerConfig {
    @ConfigurationProperties(prefix = "computer")
    @Bean
    public ComputerOp getComputerOp(){
        return new ComputerOp();
    }
}
