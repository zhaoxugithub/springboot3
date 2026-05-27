package com.atguigu.transaction.controller;

import com.atguigu.transaction.entity.QueryResult;
import com.atguigu.transaction.service.PerfTestService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 性能测试接口
 *
 * <pre>
 * ===================== 初始化 =====================
 * POST /perf/init          — 异步触发存储过程插入100万条数据
 * GET  /perf/count         — 查询当前数据量
 * DELETE /perf/truncate    — 清空表
 *
 * ===================== 索引管理 =====================
 * POST /perf/index/add?column=age    — 给 age 列加索引
 * POST /perf/index/add?column=city   — 给 city 列加索引
 * POST /perf/index/remove?column=age — 删除 age 索引
 *
 * ===================== 查询测试 =====================
 * GET /perf/query/single/age?minAge=20&maxAge=30&limit=10000
 *     — 单线程按年龄范围查询
 *
 * GET /perf/query/single/city?city=北京&limit=10000
 *     — 单线程按城市查询
 *
 * GET /perf/query/parallel/cities?threadCount=10&limit=5000
 *     — 多线程并行查询所有城市（每个城市一个线程）
 *
 * GET /perf/query/parallel/age-sharding?threadCount=5&limit=20000
 *     — 多线程按年龄分片并行查询（分治）
 *
 * ===================== 索引对比 =====================
 * GET /perf/index/compare?column=city&value=北京&limit=100000
 *     — 自动对比有索引和无索引的查询耗时（column 支持 city / age）
 * </pre>
 */
@RestController
@RequestMapping("/perf")
public class PerfTestController {

    @Resource
    private PerfTestService perfTestService;

    // ----------------------------------------------------------------
    // 初始化
    // ----------------------------------------------------------------

    @PostMapping("/init")
    public String initData() {
        return perfTestService.initData();
    }

    @GetMapping("/count")
    public Map<String, Object> count() {
        long cnt = perfTestService.count();
        return Map.of("count", cnt, "msg", cnt >= 1_000_000 ? "数据已就绪" : "数据初始化中，当前：" + cnt);
    }

    @DeleteMapping("/truncate")
    public String truncate() {
        return perfTestService.truncate();
    }

    // ----------------------------------------------------------------
    // 索引管理
    // ----------------------------------------------------------------

    @PostMapping("/index/add")
    public String addIndex(@RequestParam String column) {
        return perfTestService.addIndex(column);
    }

    @PostMapping("/index/remove")
    public String removeIndex(@RequestParam String column) {
        return perfTestService.removeIndex(column);
    }

    // ----------------------------------------------------------------
    // 单线程查询
    // ----------------------------------------------------------------

    @GetMapping("/query/single/age")
    public QueryResult singleQueryByAge(
            @RequestParam(defaultValue = "20") int minAge,
            @RequestParam(defaultValue = "30") int maxAge,
            @RequestParam(defaultValue = "10000") int limit) {
        return perfTestService.singleQueryByAge(minAge, maxAge, limit);
    }

    @GetMapping("/query/single/city")
    public QueryResult singleQueryByCity(
            @RequestParam(defaultValue = "北京") String city,
            @RequestParam(defaultValue = "10000") int limit) {
        return perfTestService.singleQueryByCity(city, limit);
    }

    // ----------------------------------------------------------------
    // 多线程并行查询
    // ----------------------------------------------------------------

    /**
     * 多线程并行查询所有城市（每个城市提交一个任务，共10个任务）
     */
    @GetMapping("/query/parallel/cities")
    public QueryResult parallelQueryByCities(
            @RequestParam(defaultValue = "10") int threadCount,
            @RequestParam(defaultValue = "5000") int limit) throws InterruptedException {
        return perfTestService.parallelQueryByAllCities(threadCount, limit);
    }

    /**
     * 多线程按年龄分片并行查询（将年龄范围分成 threadCount 段）
     */
    @GetMapping("/query/parallel/age-sharding")
    public QueryResult parallelQueryByAgeSharding(
            @RequestParam(defaultValue = "5") int threadCount,
            @RequestParam(defaultValue = "20000") int limit) throws InterruptedException {
        return perfTestService.parallelQueryByAgeSharding(threadCount, limit);
    }

    // ----------------------------------------------------------------
    // 索引 vs 无索引 对比
    // ----------------------------------------------------------------

    /**
     * 自动对比有索引和无索引的查询耗时
     *
     * @param column 列名，支持 city 或 age
     * @param value  查询值（city时填城市名；age时填起始年龄数字）
     * @param limit  limit 条数
     */
    @GetMapping("/index/compare")
    public Map<String, QueryResult> indexCompare(
            @RequestParam(defaultValue = "city") String column,
            @RequestParam(defaultValue = "北京") String value,
            @RequestParam(defaultValue = "100000") int limit) {
        return perfTestService.indexCompare(column, value, limit);
    }
}

