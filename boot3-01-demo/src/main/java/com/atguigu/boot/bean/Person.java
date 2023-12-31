package com.atguigu.boot.bean;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lfy
 * @Description
 * @create 2023-03-30 9:12
 */
@Component
@ConfigurationProperties(prefix = "person")
@Data
// 和配置文件person前缀的所有配置进行绑定
// 自动生成JavaBean属性的getter/setter
//@NoArgsConstructor //自动生成无参构造器
// 自动生成全参构造器
// @AllArgsConstructor
public class Person {
    private String name;
    private Integer age;
    private Date birthDay;
    private Boolean like;
    /**
     * 嵌套对象
     */
    private Child child;
    /**
     * 数组（里面是对象）
     */
    private List<Dog> dogs;
    /**
     * 表示Map
     */
    private Map<String, Cat> cats;
}
