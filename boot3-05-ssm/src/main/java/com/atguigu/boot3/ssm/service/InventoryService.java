package com.atguigu.boot3.ssm.service;

import com.atguigu.boot3.ssm.bean.Inventory;
import com.atguigu.boot3.ssm.mapper.InventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 库存服务（方法二：基于 SELECT ... FOR UPDATE 悲观锁）
 *
 * 核心原理：
 * - 在事务中执行 SELECT ... FOR UPDATE 锁定目标行
 * - 其他事务的 SELECT ... FOR UPDATE 会阻塞等待，直到当前事务提交
 * - 利用 MySQL InnoDB 行级锁保证并发安全
 */
@Slf4j
@Service
public class InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    /**
     * 悲观锁扣减库存
     *
     * @param productId 商品 ID
     * @param quantity  扣减数量
     * @return true=扣减成功, false=库存不足
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStockWithPessimisticLock(Long productId, int quantity) {
        log.info("🔒 尝试获取悲观锁，查询库存：productId={}", productId);

        // SELECT ... FOR UPDATE 会对该行加 X 锁（排他锁）
        // 其他事务的 SELECT ... FOR UPDATE 在此处阻塞等待
        Inventory inventory = inventoryMapper.selectForUpdate(productId);

        if (inventory == null) {
            log.error("❌ 商品不存在：productId={}", productId);
            return false;
        }

        int currentStock = inventory.getStock();
        log.info("📦 当前库存：productId={}, stock={}, 扣减数量={}", productId, currentStock, quantity);

        if (currentStock < quantity) {
            log.warn("❌ 库存不足：productId={}, currentStock={}, need={}", productId, currentStock, quantity);
            return false;
        }

        int newStock = currentStock - quantity;
        inventoryMapper.updateStock(productId, newStock);
        log.info("✅ 扣减成功：productId={}, stock {} → {}", productId, currentStock, newStock);

        // 事务提交时自动释放行锁
        return true;
    }
}
