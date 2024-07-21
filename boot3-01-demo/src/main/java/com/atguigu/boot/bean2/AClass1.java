package com.atguigu.boot.bean2;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


// 这里的aclass1 只能小写
@Component
@ConfigurationProperties(prefix = "aclass1")
@Data
public class AClass1 {
    private String name;
    private String describe;
}
