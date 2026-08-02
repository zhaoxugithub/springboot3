package com.atguigu.boot3.ssm.bean;

import lombok.Data;

/**
 * 库存实体（方法二：悲观锁）
 */
@Data
public class Inventory {
    private Long id;
    private String productName;
    private Integer stock;
}
