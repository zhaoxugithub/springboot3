package com.atguigu.boot3.ssm.service;

import com.atguigu.boot3.ssm.bean.DistributedLockRecord;
import com.atguigu.boot3.ssm.mapper.DistributedLockMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 分布式锁服务（方法一：基于唯一索引 INSERT）
 *
 * 核心原理：
 * - 加锁：INSERT 一条 lock_key=xxx 的记录，利用 UNIQUE 约束保证互斥
 * - 解锁：DELETE 按 lock_key + owner 删除，防止误删他人持有的锁
 * - 防死锁：expire_time 过期机制，定时清理过期锁
 */
@Slf4j
@Service
public class DistributedLockService {

    @Autowired
    private DistributedLockMapper distributedLockMapper;

    /**
     * 尝试获取锁
     *
     * @param lockKey       锁的唯一标识
     * @param owner         锁持有者（如 UUID、主机名），用于防止误释放
     * @param expireSeconds 锁过期时间（秒），防止死锁
     * @return true=获取成功, false=锁被占用
     */
    public boolean tryLock(String lockKey, String owner, int expireSeconds) {
        LocalDateTime expireTime = LocalDateTime.now().plusSeconds(expireSeconds);
        try {
            distributedLockMapper.insertLock(lockKey, owner, expireTime);
            log.info("✅ 获取锁成功：lockKey={}, owner={}, expireTime={}", lockKey, owner, expireTime);
            return true;
        } catch (DuplicateKeyException e) {
            // 唯一键冲突 → 锁已被他人持有
            // 但需要检查已有锁是否过期
            DistributedLockRecord existing = distributedLockMapper.getLock(lockKey);
            if (existing != null && existing.getExpireTime().isBefore(LocalDateTime.now())) {
                // 锁已过期，清理后重试
                log.warn("⚠️ 检测到过期锁，尝试清理：lockKey={}, expiredAt={}", lockKey, existing.getExpireTime());
                distributedLockMapper.deleteExpiredLocks();
                // 重试一次
                try {
                    distributedLockMapper.insertLock(lockKey, owner, expireTime);
                    log.info("✅ 清理过期锁后获取成功：lockKey={}", lockKey);
                    return true;
                } catch (DuplicateKeyException ex) {
                    log.info("❌ 重试失败，锁被占用：lockKey={}", lockKey);
                    return false;
                }
            }
            log.info("❌ 锁被占用：lockKey={}", lockKey);
            return false;
        }
    }

    /**
     * 尝试获取锁（使用默认 UUID 作为 owner）
     */
    public boolean tryLock(String lockKey, int expireSeconds) {
        return tryLock(lockKey, UUID.randomUUID().toString(), expireSeconds);
    }

    /**
     * 释放锁
     *
     * @param lockKey 锁的唯一标识
     * @param owner   锁持有者
     * @return true=释放成功, false=锁不存在或不属于当前持有者
     */
    public boolean unlock(String lockKey, String owner) {
        int deleted = distributedLockMapper.deleteLock(lockKey, owner);
        if (deleted > 0) {
            log.info("🔓 释放锁成功：lockKey={}, owner={}", lockKey, owner);
            return true;
        }
        log.warn("🔓 释放锁失败（锁不存在或不属于当前持有者）：lockKey={}, owner={}", lockKey, owner);
        return false;
    }

    /**
     * 查询锁状态
     *
     * @param lockKey 锁的唯一标识
     * @return 锁记录，null 表示锁空闲
     */
    public DistributedLockRecord getLockStatus(String lockKey) {
        return distributedLockMapper.getLock(lockKey);
    }

    /**
     * 清理所有过期锁
     */
    public int cleanExpiredLocks() {
        int count = distributedLockMapper.deleteExpiredLocks();
        if (count > 0) {
            log.info("🧹 清理过期锁：{} 条", count);
        }
        return count;
    }
}
