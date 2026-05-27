-- ============================================================
-- 性能测试表初始化脚本
-- 用于测试：多线程并行查询 & 索引 vs 非索引 查询速度对比
-- ============================================================

-- 1. 建表
DROP TABLE IF EXISTS perf_test;
CREATE TABLE perf_test (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    user_id     BIGINT       NOT NULL                COMMENT '用户ID',
    name        VARCHAR(50)  NOT NULL                COMMENT '姓名',
    age         INT          NOT NULL                COMMENT '年龄',
    email       VARCHAR(100) NOT NULL                COMMENT '邮箱',
    city        VARCHAR(50)  NOT NULL                COMMENT '城市',
    score       DECIMAL(10,2) NOT NULL DEFAULT 0     COMMENT '分数',
    status      TINYINT      NOT NULL DEFAULT 1      COMMENT '状态 1:正常 0:禁用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='性能测试表';

-- 2. 创建存储过程批量插入 100 万条数据（每批 1000 条，共 1000 批）
DROP PROCEDURE IF EXISTS insert_perf_test_data;

DELIMITER $$

CREATE PROCEDURE insert_perf_test_data()
BEGIN
    DECLARE batch     INT DEFAULT 0;   -- 当前批次
    DECLARE i         INT DEFAULT 0;   -- 批次内行号
    DECLARE total_batches INT DEFAULT 1000; -- 总批次
    DECLARE batch_size    INT DEFAULT 1000; -- 每批大小

    SET autocommit = 0;

    WHILE batch < total_batches DO
        SET i = 0;
        WHILE i < batch_size DO
            INSERT INTO perf_test (user_id, name, age, email, city, score, status, created_at)
            VALUES (
                batch * batch_size + i + 1,
                CONCAT('user_', batch * batch_size + i + 1),
                10 + MOD(batch * batch_size + i, 50),
                CONCAT('user_', batch * batch_size + i + 1, '@test.com'),
                ELT(MOD(batch * batch_size + i, 10) + 1,
                    '北京','上海','广州','深圳','杭州',
                    '成都','武汉','西安','南京','重庆'),
                ROUND(RAND() * 10000, 2),
                IF(MOD(batch * batch_size + i, 10) = 0, 0, 1),
                DATE_ADD('2020-01-01', INTERVAL FLOOR(RAND() * 2000) DAY)
            );
            SET i = i + 1;
        END WHILE;
        COMMIT;
        SET batch = batch + 1;
    END WHILE;

    SET autocommit = 1;
    SELECT CONCAT('数据插入完成，共插入 ', total_batches * batch_size, ' 条记录') AS result;
END$$

DELIMITER ;

-- 3. 执行存储过程插入数据（取消注释后执行，耗时较长约 2~5 分钟）
-- CALL insert_perf_test_data();

-- 4. 查看数据量
-- SELECT COUNT(*) FROM perf_test;

-- ============================================================
-- 索引管理 SQL（在接口中动态调用）
-- ============================================================

-- 添加 age 索引
-- CREATE INDEX idx_age ON perf_test(age);

-- 添加 city 索引
-- CREATE INDEX idx_city ON perf_test(city);

-- 添加 name 索引
-- CREATE INDEX idx_name ON perf_test(name);

-- 删除 age 索引
-- DROP INDEX idx_age ON perf_test;

-- 删除 city 索引
-- DROP INDEX idx_city ON perf_test;

-- 删除 name 索引
-- DROP INDEX idx_name ON perf_test;

