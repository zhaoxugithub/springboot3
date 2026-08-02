package com.atguigu.boot3.ssm.bean;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分布式锁记录实体（方法一：唯一索引）
 */
@Data
public class DistributedLockRecord {
    private Long id;
    private String lockKey;
    private String owner;
    private LocalDateTime expireTime;
    private LocalDateTime createdTime;
}
