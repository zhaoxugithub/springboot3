-- MySQL 分布式锁 Demo 建表脚本
-- 方法一：唯一索引实现分布式锁
CREATE TABLE IF NOT EXISTS `distributed_lock` (
    `id`           BIGINT AUTO_INCREMENT PRIMARY KEY,
    `lock_key`     VARCHAR(255) NOT NULL COMMENT '锁的唯一键',
    `owner`        VARCHAR(128) NOT NULL COMMENT '锁持有者标识（UUID或主机名）',
    `expire_time`  DATETIME     NOT NULL COMMENT '锁过期时间',
    `created_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY `uk_lock_key` (`lock_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分布式锁表（方法一：唯一索引）';

-- 方法二：悲观锁 SELECT ... FOR UPDATE 使用的库存表
CREATE TABLE IF NOT EXISTS `inventory` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `product_name` VARCHAR(255) NOT NULL COMMENT '商品名称',
    `stock`        INT          NOT NULL DEFAULT 0 COMMENT '库存数量'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表（方法二：悲观锁）';

-- 初始化库存数据
INSERT INTO `inventory` (`id`, `product_name`, `stock`) VALUES (1, 'iPhone 16', 100)
ON DUPLICATE KEY UPDATE `stock` = 100;
