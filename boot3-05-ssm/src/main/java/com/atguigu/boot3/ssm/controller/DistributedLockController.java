package com.atguigu.boot3.ssm.controller;

import com.atguigu.boot3.ssm.bean.DistributedLockRecord;
import com.atguigu.boot3.ssm.service.DistributedLockService;
import com.atguigu.boot3.ssm.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

/**
 * 分布式锁 Demo Controller
 */
@RestController
public class DistributedLockController {

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private InventoryService inventoryService;

    // ==================== 方法一：唯一索引分布式锁 ====================

    /**
     * 尝试获取锁
     * GET /lock/acquire?key=order:123&owner=server1&expireSeconds=30
     */
    @GetMapping("/lock/acquire")
    public Map<String, Object> acquire(@RequestParam("key") String key,
                                       @RequestParam(value = "owner", defaultValue = "") String owner,
                                       @RequestParam(value = "expireSeconds", defaultValue = "30") int expireSeconds) {
        String actualOwner = owner.isEmpty() ? UUID.randomUUID().toString() : owner;
        boolean acquired = lockService.tryLock(key, actualOwner, expireSeconds);
        return Map.of(
                "success", acquired,
                "lockKey", key,
                "owner", actualOwner,
                "expireSeconds", expireSeconds,
                "message", acquired ? "获取锁成功" : "锁已被占用"
        );
    }

    /**
     * 释放锁
     * GET /lock/release?key=order:123&owner=server1
     */
    @GetMapping("/lock/release")
    public Map<String, Object> release(@RequestParam("key") String key,
                                       @RequestParam("owner") String owner) {
        boolean released = lockService.unlock(key, owner);
        return Map.of(
                "success", released,
                "lockKey", key,
                "owner", owner,
                "message", released ? "释放锁成功" : "释放锁失败（锁不存在或不属于该持有者）"
        );
    }

    /**
     * 查询锁状态
     * GET /lock/status?key=order:123
     */
    @GetMapping("/lock/status")
    public Map<String, Object> status(@RequestParam("key") String key) {
        DistributedLockRecord lock = lockService.getLockStatus(key);
        if (lock == null) {
            return Map.of("locked", false, "message", "锁空闲");
        }
        return Map.of(
                "locked", true,
                "lockKey", lock.getLockKey(),
                "owner", lock.getOwner(),
                "expireTime", lock.getExpireTime().toString(),
                "createdTime", lock.getCreatedTime().toString()
        );
    }

    /**
     * 清理过期锁
     * GET /lock/clean
     */
    @GetMapping("/lock/clean")
    public Map<String, Object> clean() {
        int count = lockService.cleanExpiredLocks();
        return Map.of("success", true, "cleaned", count);
    }

    // ==================== 方法二：悲观锁扣库存 ====================

    /**
     * 悲观锁扣减库存
     * GET /inventory/deduct?productId=1&quantity=1
     */
    @GetMapping("/inventory/deduct")
    public Map<String, Object> deduct(@RequestParam(value = "productId", defaultValue = "1") Long productId,
                                      @RequestParam(value = "quantity", defaultValue = "1") int quantity) {
        boolean success = inventoryService.deductStockWithPessimisticLock(productId, quantity);
        return Map.of(
                "success", success,
                "productId", productId,
                "quantity", quantity,
                "message", success ? "扣减成功" : "扣减失败（库存不足或商品不存在）"
        );
    }
}
