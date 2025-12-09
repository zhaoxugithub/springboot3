package com.atguigu.boot.bean2;

import lombok.Data;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ToString
@Data
@ConfigurationProperties(prefix = "phone")
public class Phone {
    private String brand;
    private String cpu;
    private Double ram;
}
