package com.atguigu.boot3.ssm.mapper;

import com.atguigu.boot3.ssm.bean.DistributedLockRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * 分布式锁 Mapper（方法一：唯一索引）
 */
public interface DistributedLockMapper {

    /**
     * 插入锁记录，利用唯一索引 uk_lock_key 保证互斥
     *
     * @param lockKey    锁的 key
     * @param owner      锁持有者
     * @param expireTime 过期时间
     * @return 插入行数
     */
    int insertLock(@Param("lockKey") String lockKey,
                   @Param("owner") String owner,
                   @Param("expireTime") LocalDateTime expireTime);

    /**
     * 释放锁（按 key + owner，防止误删他人持有的锁）
     *
     * @param lockKey 锁的 key
     * @param owner   锁持有者
     * @return 删除行数
     */
    int deleteLock(@Param("lockKey") String lockKey,
                   @Param("owner") String owner);

    /**
     * 清理所有已过期的锁记录
     *
     * @return 删除行数
     */
    int deleteExpiredLocks();

    /**
     * 按 key 查询锁记录
     *
     * @param lockKey 锁的 key
     * @return 锁记录，不存在返回 null
     */
    DistributedLockRecord getLock(@Param("lockKey") String lockKey);
}
