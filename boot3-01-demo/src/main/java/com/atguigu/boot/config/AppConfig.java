package com.atguigu.boot.config;

import com.atguigu.boot.bean.Teacher;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * ClassName AppConfig
 * Description TODO
 * Author 11931
 * Date 2023-07-11:23:49
 * Version 1.0
 **/

/**
 * 表示这是一个配置类
 *
 * @author 11931
 */
@SpringBootConfiguration
@EnableConfigurationProperties(Teacher.class)
public class AppConfig {
    // public Teacher getTeacher() {
    //     return new Teacher();
    // }
}
