package com.atguigu.boot3.ssm;

import com.atguigu.boot3.ssm.mapper.InventoryMapper;
import com.atguigu.boot3.ssm.service.DistributedLockService;
import com.atguigu.boot3.ssm.service.InventoryService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MySQL 分布式锁测试类
 *
 * 测试前需确保：
 * 1. MySQL 数据库已启动（localhost:3306/test）
 * 2. 已执行 src/main/resources/sql/distributed_lock.sql 建表脚本
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DistributedLockTest {

    @Autowired
    private DistributedLockService lockService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryMapper inventoryMapper;

    private static final String TEST_KEY = "test:lock:junit";
    private static String savedOwner;

    @BeforeEach
    void setUp() {
        // 清理可能残留的过期锁
        lockService.cleanExpiredLocks();
    }

    // ======================== 方法一：唯一索引分布式锁 ========================

    @Test
    @Order(1)
    @DisplayName("方法一：基本加锁与释放")
    void testMethod1_AcquireAndRelease() {
        String owner = owner();

        // 获取锁
        boolean acquired = lockService.tryLock(TEST_KEY, owner, 30);
        assertTrue(acquired, "第一次加锁应该成功");

        // 查询状态确认锁存在
        Assertions.assertNotNull(lockService.getLockStatus(TEST_KEY), "锁应存在");

        // 释放锁
        boolean released = lockService.unlock(TEST_KEY, owner);
        assertTrue(released, "释放自己的锁应该成功");

        // 确认锁已删除
        Assertions.assertNull(lockService.getLockStatus(TEST_KEY), "释放后锁应不存在");

        savedOwner = owner;
    }

    @Test
    @Order(2)
    @DisplayName("方法一：同一 key 重复加锁应被拒绝")
    void testMethod1_SameKeyBlocked() {
        String owner1 = owner();

        // A 获取锁
        assertTrue(lockService.tryLock(TEST_KEY, owner1, 30));

        // B 尝试获取同一把锁，应失败
        String owner2 = owner();
        assertFalse(lockService.tryLock(TEST_KEY, owner2, 30), "同一 key 重复加锁应被拒绝");

        // 清理
        lockService.unlock(TEST_KEY, owner1);
        savedOwner = owner1;
    }

    @Test
    @Order(3)
    @DisplayName("方法一：不同 key 互不影响")
    void testMethod1_DifferentKeyNotBlocked() {
        String ownerA = owner();
        String ownerB = owner();

        // A 获取 lockA
        assertTrue(lockService.tryLock("test:lock:A", ownerA, 30));
        // B 获取 lockB（不同 key，不受影响）
        assertTrue(lockService.tryLock("test:lock:B", ownerB, 30));

        // 清理
        lockService.unlock("test:lock:A", ownerA);
        lockService.unlock("test:lock:B", ownerB);
    }

    @Test
    @Order(4)
    @DisplayName("方法一：释放锁时不应误删他人的锁（防误删）")
    void testMethod1_NoAccidentalDeletion() {
        String ownerA = owner();

        // A 获取锁
        assertTrue(lockService.tryLock(TEST_KEY, ownerA, 30));

        // B 尝试释放 A 的锁（用 B 的 owner）
        String ownerB = owner();
        boolean released = lockService.unlock(TEST_KEY, ownerB);
        assertFalse(released, "不应能释放他人的锁");

        // 确认锁仍然存在（属于 A）
        Assertions.assertNotNull(lockService.getLockStatus(TEST_KEY), "A 的锁仍应存在");

        // A 自己释放
        lockService.unlock(TEST_KEY, ownerA);
    }

    @Test
    @Order(5)
    @DisplayName("方法一：过期锁自动清理")
    void testMethod1_ExpiredLockCleanup() {
        String expireKey = "test:lock:expired";
        String owner = owner();

        // 获取一个 1 秒后过期的锁
        assertTrue(lockService.tryLock(expireKey, owner, 1));

        // 等待锁过期
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 清理过期锁
        int cleaned = lockService.cleanExpiredLocks();
        assertTrue(cleaned > 0, "应清理到至少 1 条过期锁");

        // 确认锁已被清理
        Assertions.assertNull(lockService.getLockStatus(expireKey), "过期锁应被清理");

        // 过期后可以重新获取
        String newOwner = owner();
        assertTrue(lockService.tryLock(expireKey, newOwner, 30), "清理后应能重新获取锁");
        lockService.unlock(expireKey, newOwner);
    }

    @Test
    @Order(6)
    @DisplayName("方法一：多线程并发争抢同一把锁")
    void testMethod1_ConcurrentAccess() throws InterruptedException {
        String key = "test:lock:concurrent";
        int threadCount = 20;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            int index = i;
            executor.submit(() -> {
                try {
                    String owner = "thread-" + index + "-" + UUID.randomUUID().toString().substring(0, 8);
                    boolean acquired = lockService.tryLock(key, owner, 10);
                    if (acquired) {
                        successCount.incrementAndGet();
                        // 模拟业务处理
                        Thread.sleep(20);
                        lockService.unlock(key, owner);
                    } else {
                        failCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("===== 并发测试结果 =====");
        System.out.println("总线程数：" + threadCount);
        System.out.println("成功获取锁：" + successCount.get() + " 次（可能多次——同一个线程获取、释放后另一个线程再获取）");
        System.out.println("锁被占用：" + failCount.get() + " 次");
        System.out.println("========================");

        // 至少应有一次成功获取
        assertTrue(successCount.get() >= 1, "至少应有一个线程成功获取锁");
    }

    // ======================== 方法二：悲观锁扣库存 ========================

    @Test
    @Order(7)
    @DisplayName("方法二：悲观锁扣减库存基本流程")
    void testMethod2_PessimisticLockDeduct() {
        // 重置库存到 100
        inventoryMapper.updateStock(1L, 100);

        // 第一次扣减
        assertTrue(inventoryService.deductStockWithPessimisticLock(1L, 1),
                "库存充足时应扣减成功");

        // 第二次扣减
        assertTrue(inventoryService.deductStockWithPessimisticLock(1L, 1),
                "库存充足时应扣减成功");
    }

    @Test
    @Order(8)
    @DisplayName("方法二：多线程并发扣库存，总扣减数应等于线程数（悲观锁保证一致性）")
    void testMethod2_ConcurrentDeduct() throws InterruptedException {
        // 重置库存到足够多
        inventoryMapper.updateStock(1L, 200);

        int threadCount = 50;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean deducted = inventoryService.deductStockWithPessimisticLock(1L, 1);
                    if (deducted) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        System.out.println("===== 悲观锁并发扣库存测试 =====");
        System.out.println("总线程数：" + threadCount);
        System.out.println("扣减成功：" + successCount.get());
        System.out.println("扣减失败：" + failCount.get());
        System.out.println("===============================");

        // 所有线程都应扣减成功（库存 200 足够 50 个线程各扣 1）
        assertEquals(threadCount, successCount.get(), "库存充足时所有线程都应扣减成功");
        assertEquals(0, failCount.get(), "不应有失败");
    }

    // ======================== 辅助方法 ========================

    private String owner() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
