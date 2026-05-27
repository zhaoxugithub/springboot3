package com.atguigu.transaction.service;

import com.atguigu.transaction.entity.PerfTest;
import com.atguigu.transaction.entity.QueryResult;
import com.atguigu.transaction.mapper.PerfTestMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class PerfTestService {

    @Resource
    private PerfTestMapper perfTestMapper;

    // 城市列表（与SQL脚本中保持一致）
    private static final String[] CITIES = {
            "北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "西安", "南京", "重庆"
    };

    // ----------------------------------------------------------------
    // 初始化：调用存储过程插入100万条数据
    // ----------------------------------------------------------------

    /**
     * 异步触发存储过程插入100万条数据（耗时较长，异步执行，立即返回）
     */
    public String initData() {
        new Thread(() -> {
            log.info("开始执行存储过程插入100万条数据...");
            long start = System.currentTimeMillis();
            try {
                perfTestMapper.callInsertProcedure();
                long cost = System.currentTimeMillis() - start;
                log.info("100万条数据插入完成，耗时 {} ms", cost);
            } catch (Exception e) {
                log.error("数据插入失败", e);
            }
        }, "data-init-thread").start();
        return "数据初始化任务已启动（异步执行），请稍后通过 /perf/count 查看数据量";
    }

    /**
     * 清空表
     */
    public String truncate() {
        perfTestMapper.truncateTable();
        return "表已清空";
    }

    /**
     * 查询总数据量
     */
    public long count() {
        return perfTestMapper.countAll();
    }

    // ----------------------------------------------------------------
    // 索引管理
    // ----------------------------------------------------------------

    public String addIndex(String column) {
        String indexName = "idx_" + column;
        try {
            perfTestMapper.createIndex(indexName, column);
            return "索引 " + indexName + " 创建成功";
        } catch (Exception e) {
            return "索引创建失败（可能已存在）：" + e.getMessage();
        }
    }

    public String removeIndex(String column) {
        String indexName = "idx_" + column;
        try {
            perfTestMapper.dropIndex(indexName);
            return "索引 " + indexName + " 删除成功";
        } catch (Exception e) {
            return "索引删除失败（可能不存在）：" + e.getMessage();
        }
    }

    // ----------------------------------------------------------------
    // 单线程查询（用于对比基准）
    // ----------------------------------------------------------------

    /**
     * 单线程按年龄范围查询（可用于有索引 vs 无索引对比）
     */
    public QueryResult singleQueryByAge(int minAge, int maxAge, int limit) {
        long start = System.currentTimeMillis();
        List<PerfTest> result = perfTestMapper.selectByAgeRange(minAge, maxAge, limit);
        long cost = System.currentTimeMillis() - start;

        return QueryResult.builder()
                .description(String.format("单线程按年龄[%d~%d]查询，limit=%d", minAge, maxAge, limit))
                .parallel(false)
                .threadCount(1)
                .totalCostMs(cost)
                .totalRows(result.size())
                .suggestion("对比有无 idx_age 索引的耗时差异")
                .build();
    }

    /**
     * 单线程按城市查询（可用于有索引 vs 无索引对比）
     */
    public QueryResult singleQueryByCity(String city, int limit) {
        long start = System.currentTimeMillis();
        List<PerfTest> result = perfTestMapper.selectByCity(city, limit);
        long cost = System.currentTimeMillis() - start;

        return QueryResult.builder()
                .description(String.format("单线程按城市[%s]查询，limit=%d", city, limit))
                .parallel(false)
                .threadCount(1)
                .totalCostMs(cost)
                .totalRows(result.size())
                .suggestion("对比有无 idx_city 索引的耗时差异")
                .build();
    }

    // ----------------------------------------------------------------
    // 多线程并行查询（固定线程池）
    // ----------------------------------------------------------------

    /**
     * 多线程并行查询：同时查询所有城市，使用 threadCount 个线程并发执行
     *
     * @param threadCount 线程数（建议 2~20）
     * @param limit       每个城市查询条数上限
     */
    public QueryResult parallelQueryByAllCities(int threadCount, int limit) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        long totalStart = System.currentTimeMillis();

        // 提交任务：每个城市一个查询任务
        for (String city : CITIES) {
            futures.add(executor.submit(() -> {
                long taskStart = System.currentTimeMillis();
                List<PerfTest> rows = perfTestMapper.selectByCity(city, limit);
                long taskCost = System.currentTimeMillis() - taskStart;

                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("city", city);
                detail.put("rows", rows.size());
                detail.put("costMs", taskCost);
                detail.put("thread", Thread.currentThread().getName());
                return detail;
            }));
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long totalCost = System.currentTimeMillis() - totalStart;

        // 收集结果
        List<Map<String, Object>> details = new ArrayList<>();
        long totalRows = 0;
        for (Future<Map<String, Object>> f : futures) {
            try {
                Map<String, Object> detail = f.get();
                totalRows += (Integer) detail.get("rows");
                details.add(detail);
            } catch (ExecutionException e) {
                log.error("子任务执行异常", e);
            }
        }

        return QueryResult.builder()
                .description(String.format("多线程并行查询所有城市（%d个城市），线程数=%d，每城市limit=%d",
                        CITIES.length, threadCount, limit))
                .parallel(true)
                .threadCount(threadCount)
                .totalCostMs(totalCost)
                .totalRows(totalRows)
                .taskDetails(details)
                .suggestion("增大线程数可减少总耗时，但受限于数据库连接池和CPU核数")
                .build();
    }

    /**
     * 多线程并行查询：按年龄段分片，多线程并发查询
     * 将 0~59 岁分成 threadCount 段并行查询
     *
     * @param threadCount 线程数（分片数）
     * @param limit       每个分片查询条数上限
     */
    public QueryResult parallelQueryByAgeSharding(int threadCount, int limit) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<Map<String, Object>>> futures = new ArrayList<>();

        int totalAgeRange = 60; // age 范围 10~59，共 50 个值
        int shardSize = totalAgeRange / threadCount;

        long totalStart = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int minAge = 10 + i * shardSize;
            final int maxAge = (i == threadCount - 1) ? 59 : (10 + (i + 1) * shardSize - 1);

            futures.add(executor.submit(() -> {
                long taskStart = System.currentTimeMillis();
                List<PerfTest> rows = perfTestMapper.selectByAgeRange(minAge, maxAge, limit);
                long taskCost = System.currentTimeMillis() - taskStart;

                Map<String, Object> detail = new LinkedHashMap<>();
                detail.put("ageRange", minAge + "~" + maxAge);
                detail.put("rows", rows.size());
                detail.put("costMs", taskCost);
                detail.put("thread", Thread.currentThread().getName());
                return detail;
            }));
        }

        executor.shutdown();
        executor.awaitTermination(60, TimeUnit.SECONDS);

        long totalCost = System.currentTimeMillis() - totalStart;

        List<Map<String, Object>> details = new ArrayList<>();
        long totalRows = 0;
        for (Future<Map<String, Object>> f : futures) {
            try {
                Map<String, Object> detail = f.get();
                totalRows += (Integer) detail.get("rows");
                details.add(detail);
            } catch (ExecutionException e) {
                log.error("子任务执行异常", e);
            }
        }

        return QueryResult.builder()
                .description(String.format("多线程按年龄分片并行查询，线程数=%d，每片limit=%d", threadCount, limit))
                .parallel(true)
                .threadCount(threadCount)
                .totalCostMs(totalCost)
                .totalRows(totalRows)
                .taskDetails(details)
                .suggestion("与单线程全量查询对比可以看出多线程分治加速效果")
                .build();
    }

    // ----------------------------------------------------------------
    // 索引对比：自动加/删索引，执行查询，返回对比结果
    // ----------------------------------------------------------------

    /**
     * 一次性对比有索引和无索引的查询速度（自动管理索引，查询完恢复原状）
     */
    public Map<String, QueryResult> indexCompare(String column, String queryValue, int limit) {
        Map<String, QueryResult> result = new LinkedHashMap<>();

        // --- 无索引查询 ---
        try { perfTestMapper.dropIndex("idx_" + column); } catch (Exception ignored) {}

        long start = System.currentTimeMillis();
        List<PerfTest> noIndexRows = "city".equals(column)
                ? perfTestMapper.selectByCity(queryValue, limit)
                : perfTestMapper.selectByAgeRange(Integer.parseInt(queryValue), Integer.parseInt(queryValue) + 5, limit);
        long noIndexCost = System.currentTimeMillis() - start;

        result.put("无索引", QueryResult.builder()
                .description("无索引查询 column=" + column + " value=" + queryValue)
                .parallel(false)
                .hasIndex(false)
                .totalCostMs(noIndexCost)
                .totalRows(noIndexRows.size())
                .build());

        // --- 加索引后查询 ---
        try { perfTestMapper.createIndex("idx_" + column, column); } catch (Exception ignored) {}

        start = System.currentTimeMillis();
        List<PerfTest> indexRows = "city".equals(column)
                ? perfTestMapper.selectByCity(queryValue, limit)
                : perfTestMapper.selectByAgeRange(Integer.parseInt(queryValue), Integer.parseInt(queryValue) + 5, limit);
        long indexCost = System.currentTimeMillis() - start;

        result.put("有索引", QueryResult.builder()
                .description("有索引查询 column=" + column + " value=" + queryValue)
                .parallel(false)
                .hasIndex(true)
                .totalCostMs(indexCost)
                .totalRows(indexRows.size())
                .suggestion(String.format("索引提速 %.2f 倍", noIndexCost == 0 ? 1.0 : (double) noIndexCost / indexCost))
                .build());

        return result;
    }
}

