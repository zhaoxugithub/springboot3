package com.atguigu.boot.config;

import com.atguigu.boot.bean.Student;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ClassName StudentConfig
 * Description TODO
 * Author 11931
 * Date 2023-07-26:18:09
 * Version 1.0
 *
 * @author 11931
 */
@Configuration
// EnableConfigurationProperties注解可以把student这个对象注入到容器里面
// 一般用作导入第三方包
@EnableConfigurationProperties(Student.class)
public class StudentConfig {
}
