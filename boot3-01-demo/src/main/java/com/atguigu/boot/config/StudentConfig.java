package com.atguigu.boot.config;

import com.atguigu.boot.bean.Student;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName StudentConfig
 * Description TODO
 * Author 11931
 * Date 2023-07-26:18:09
 * Version 1.0
 **/
@Configuration
@EnableConfigurationProperties(Student.class)
public class StudentConfig {
}
