# PerfTestController 性能测试报告

> 生成时间：2026-05-28 23:01:22  
> 测试工具：Python requests + ThreadPoolExecutor  

## 测试环境

| 项目 | 说明 |
|------|------|
| 数据库 | MySQL (192.168.150.151:3306/demo) |
| 数据量 | **8,000,000 条** |
| 应用端口 | 8080 |
| 测试方式 | Python HTTP → localhost |

---
## 一、单线程查询

| 测试场景 | 服务端耗时 | 客户端耗时 | 返回行数 | 吞吐量(rows/s) |
|----------|-----------|-----------|---------|---------------|
| age_20_30_L10k | 269ms | 284ms | 10,000 | 37,174 |
| age_20_30_L50k | 1517ms | 1532ms | 50,000 | 32,959 |
| age_20_30_L100k | 3995ms | 4010ms | 100,000 | 25,031 |
| age_18_60_L10k | 262ms | 277ms | 10,000 | 38,167 |
| city_beijing_L10k | 110ms | 114ms | 10,000 | 90,909 |
| city_beijing_L50k | 564ms | 568ms | 50,000 | 88,652 |
| city_beijing_L100k | 1327ms | 1343ms | 100,000 | 75,357 |
| city_shanghai_L10k | 128ms | 131ms | 10,000 | 78,125 |

---
## 二、多线程并行查询

| 测试场景 | 线程数 | 服务端耗时 | 客户端耗时 | 总行数 | 吞吐量(rows/s) |
|----------|--------|-----------|-----------|--------|---------------|
| cities_t5_L5k | 5 | 155ms | 182ms | 50,000 | 322,580 |
| cities_t10_L5k | 10 | 133ms | 149ms | 50,000 | 375,939 |
| cities_t10_L10k | 10 | 252ms | 273ms | 100,000 | 396,825 |
| cities_t20_L10k | 20 | 235ms | 250ms | 100,000 | 425,531 |
| age_shard_t5_L20k | 5 | 670ms | 698ms | 100,000 | 149,253 |
| age_shard_t10_L20k | 10 | 478ms | 493ms | 180,000 | 376,569 |
| age_shard_t10_L50k | 10 | 2076ms | 2098ms | 450,000 | 216,763 |
| age_shard_t20_L20k | 20 | 1031ms | 1036ms | 340,000 | 329,776 |

---
## 三、索引对比

| 测试场景 | 无索引 | 有索引 | 提速比 | 总耗时(含DDL) |
|----------|--------|--------|--------|-------------|
| idx_city_L10k | 131ms | 152ms | 索引提速 0.86 倍x | 14.93s |
| idx_city_L50k | 779ms | 697ms | 索引提速 1.12 倍x | 16.14s |
| idx_city_L100k | 1449ms | 1582ms | 索引提速 0.92 倍x | 17.71s |
| idx_age_L10k | 145ms | 352ms | 索引提速 0.41 倍x | 11.94s |
| idx_age_L100k | 1291ms | 4141ms | 索引提速 0.31 倍x | 16.84s |

---
## 四、并发压力测试

| 并发数 | 总耗时 | 平均响应 | 最大响应 | 最小响应 | 成功率 |
|--------|--------|---------|---------|---------|--------|
| 5 | 36ms | 33ms | 34ms | 32ms | 5/5 |
| 10 | 32ms | 23ms | 28ms | 19ms | 10/10 |
| 20 | 54ms | 31ms | 39ms | 23ms | 20/20 |
| 50 | 129ms | 52ms | 83ms | 24ms | 50/50 |

---
## 五、结论与建议

### 关键发现

- 最快单线程查询：**city_beijing_L10k** → 110ms
- 最慢单线程查询：**age_20_30_L100k** → 3995ms
- 最高效并行查询：**cities_t10_L5k** → 133ms (50,000行)

### 优化建议

1. **limit≤10000** 时无索引即可满足 <200ms 的响应
2. **大数据量(limit≥50000)** 优先使用多线程分片
3. **索引策略**：等值+小limit有效；范围+高命中率时索引反而有害
4. **覆盖索引**：避免回表，可根据实际查询字段建联合索引
5. **分页优化**：超大 limit 建议改用 cursor-based 分页

---
## 附录：原始数据

<details><summary>点击展开 JSON</summary>

```json
[
  {
    "name": "data_count",
    "category": "init",
    "client_ms": 1540.02,
    "success": true,
    "server_data": {
      "msg": "数据已就绪",
      "count": 8000000
    }
  },
  {
    "name": "age_20_30_L10k",
    "category": "single",
    "client_ms": 284.18,
    "success": true,
    "server_data": {
      "description": "单线程按年龄[20~30]查询，limit=10000",
      "totalCostMs": 269,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 10000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_age 索引的耗时差异"
    }
  },
  {
    "name": "age_20_30_L50k",
    "category": "single",
    "client_ms": 1531.74,
    "success": true,
    "server_data": {
      "description": "单线程按年龄[20~30]查询，limit=50000",
      "totalCostMs": 1517,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 50000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_age 索引的耗时差异"
    }
  },
  {
    "name": "age_20_30_L100k",
    "category": "single",
    "client_ms": 4009.66,
    "success": true,
    "server_data": {
      "description": "单线程按年龄[20~30]查询，limit=100000",
      "totalCostMs": 3995,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 100000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_age 索引的耗时差异"
    }
  },
  {
    "name": "age_18_60_L10k",
    "category": "single",
    "client_ms": 276.61,
    "success": true,
    "server_data": {
      "description": "单线程按年龄[18~60]查询，limit=10000",
      "totalCostMs": 262,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 10000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_age 索引的耗时差异"
    }
  },
  {
    "name": "city_beijing_L10k",
    "category": "single",
    "client_ms": 113.88,
    "success": true,
    "server_data": {
      "description": "单线程按城市[北京]查询，limit=10000",
      "totalCostMs": 110,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 10000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_city 索引的耗时差异"
    }
  },
  {
    "name": "city_beijing_L50k",
    "category": "single",
    "client_ms": 567.9,
    "success": true,
    "server_data": {
      "description": "单线程按城市[北京]查询，limit=50000",
      "totalCostMs": 564,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 50000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_city 索引的耗时差异"
    }
  },
  {
    "name": "city_beijing_L100k",
    "category": "single",
    "client_ms": 1342.98,
    "success": true,
    "server_data": {
      "description": "单线程按城市[北京]查询，limit=100000",
      "totalCostMs": 1327,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 100000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_city 索引的耗时差异"
    }
  },
  {
    "name": "city_shanghai_L10k",
    "category": "single",
    "client_ms": 131.3,
    "success": true,
    "server_data": {
      "description": "单线程按城市[上海]查询，limit=10000",
      "totalCostMs": 128,
      "parallel": false,
      "threadCount": 1,
      "hasIndex": false,
      "totalRows": 10000,
      "taskDetails": null,
      "suggestion": "对比有无 idx_city 索引的耗时差异"
    }
  },
  {
    "name": "cities_t5_L5k",
    "category": "parallel",
    "client_ms": 181.73,
    "success": true,
    "server_data": {
      "description": "多线程并行查询所有城市（10个城市），线程数=5，每城市limit=5000",
      "totalCostMs": 155,
      "parallel": true,
      "threadCount": 5,
      "hasIndex": false,
      "totalRows": 50000,
      "taskDetails": [
        {
          "city": "北京",
          "rows": 5000,
          "costMs": 63,
          "thread": "pool-6-thread-1"
        },
        {
          "city": "上海",
          "rows": 5000,
          "costMs": 68,
          "thread": "pool-6-thread-2"
        },
        {
          "city": "广州",
          "rows": 5000,
          "costMs": 74,
          "thread": "pool-6-thread-3"
        },
        {
          "city": "深圳",
          "rows": 5000,
          "costMs": 77,
          "thread": "pool-6-thread-4"
        },
        {
          "city": "杭州",
          "rows": 5000,
          "costMs": 73,
          "thread": "pool-6-thread-5"
        },
        {
          "city": "成都",
          "rows": 5000,
          "costMs": 59,
          "thread": "pool-6-thread-1"
        },
        {
          "city": "武汉",
          "rows": 5000,
          "costMs": 76,
          "thread": "pool-6-thread-2"
        },
        {
          "city": "西安",
          "rows": 5000,
          "costMs": 70,
          "thread": "pool-6-thread-5"
        },
        {
          "city": "南京",
          "rows": 5000,
          "costMs": 68,
          "thread": "pool-6-thread-3"
        },
        {
          "city": "重庆",
          "rows": 5000,
          "costMs": 76,
          "thread": "pool-6-thread-4"
        }
      ],
      "suggestion": "增大线程数可减少总耗时，但受限于数据库连接池和CPU核数"
    }
  },
  {
    "name": "cities_t10_L5k",
    "category": "parallel",
    "client_ms": 149.23,
    "success": true,
    "server_data": {
      "description": "多线程并行查询所有城市（10个城市），线程数=10，每城市limit=5000",
      "totalCostMs": 133,
      "parallel": true,
      "threadCount": 10,
      "hasIndex": false,
      "totalRows": 50000,
      "taskDetails": [
        {
          "city": "北京",
          "rows": 5000,
          "costMs": 110,
          "thread": "pool-7-thread-1"
        },
        {
          "city": "上海",
          "rows": 5000,
          "costMs": 131,
          "thread": "pool-7-thread-2"
        },
        {
          "city": "广州",
          "rows": 5000,
          "costMs": 133,
          "thread": "pool-7-thread-3"
        },
        {
          "city": "深圳",
          "rows": 5000,
          "costMs": 125,
          "thread": "pool-7-thread-4"
        },
        {
          "city": "杭州",
          "rows": 5000,
          "costMs": 121,
          "thread": "pool-7-thread-5"
        },
        {
          "city": "成都",
          "rows": 5000,
          "costMs": 129,
          "thread": "pool-7-thread-6"
        },
        {
          "city": "武汉",
          "rows": 5000,
          "costMs": 101,
          "thread": "pool-7-thread-7"
        },
        {
          "city": "西安",
          "rows": 5000,
          "costMs": 124,
          "thread": "pool-7-thread-8"
        },
        {
          "city": "南京",
          "rows": 5000,
          "costMs": 117,
          "thread": "pool-7-thread-9"
        },
        {
          "city": "重庆",
          "rows": 5000,
          "costMs": 120,
          "thread": "pool-7-thread-10"
        }
      ],
      "suggestion": "增大线程数可减少总耗时，但受限于数据库连接池和CPU核数"
    }
  },
  {
    "name": "cities_t10_L10k",
    "category": "parallel",
    "client_ms": 272.61,
    "success": true,
    "server_data": {
      "description": "多线程并行查询所有城市（10个城市），线程数=10，每城市limit=10000",
      "totalCostMs": 252,
      "parallel": true,
      "threadCount": 10,
      "hasIndex": false,
      "totalRows": 100000,
      "taskDetails": [
        {
          "city": "北京",
          "rows": 10000,
          "costMs": 211,
          "thread": "pool-8-thread-1"
        },
        {
          "city": "上海",
          "rows": 10000,
          "costMs": 229,
          "thread": "pool-8-thread-2"
        },
        {
          "city": "广州",
          "rows": 10000,
          "costMs": 250,
          "thread": "pool-8-thread-3"
        },
        {
          "city": "深圳",
          "rows": 10000,
          "costMs": 222,
          "thread": "pool-8-thread-4"
        },
        {
          "city": "杭州",
          "rows": 10000,
          "costMs": 214,
          "thread": "pool-8-thread-5"
        },
        {
          "city": "成都",
          "rows": 10000,
          "costMs": 244,
          "thread": "pool-8-thread-6"
        },
        {
          "city": "武汉",
          "rows": 10000,
          "costMs": 206,
          "thread": "pool-8-thread-7"
        },
        {
          "city": "西安",
          "rows": 10000,
          "costMs": 244,
          "thread": "pool-8-thread-8"
        },
        {
          "city": "南京",
          "rows": 10000,
          "costMs": 227,
          "thread": "pool-8-thread-9"
        },
        {
          "city": "重庆",
          "rows": 10000,
          "costMs": 202,
          "thread": "pool-8-thread-10"
        }
      ],
      "suggestion": "增大线程数可减少总耗时，但受限于数据库连接池和CPU核数"
    }
  },
  {
    "name": "cities_t20_L10k",
    "category": "parallel",
    "client_ms": 249.95,
    "success": true,
    "server_data": {
      "description": "多线程并行查询所有城市（10个城市），线程数=20，每城市limit=10000",
      "totalCostMs": 235,
      "parallel": true,
      "threadCount": 20,
      "hasIndex": false,
      "totalRows": 100000,
      "taskDetails": [
        {
          "city": "北京",
          "rows": 10000,
          "costMs": 215,
          "thread": "pool-9-thread-1"
        },
        {
          "city": "上海",
          "rows": 10000,
          "costMs": 211,
          "thread": "pool-9-thread-2"
        },
        {
          "city": "广州",
          "rows": 10000,
          "costMs": 235,
          "thread": "pool-9-thread-3"
        },
        {
          "city": "深圳",
          "rows": 10000,
          "costMs": 224,
          "thread": "pool-9-thread-4"
        },
        {
          "city": "杭州",
          "rows": 10000,
          "costMs": 207,
          "thread": "pool-9-thread-5"
        },
        {
          "city": "成都",
          "rows": 10000,
          "costMs": 234,
          "thread": "pool-9-thread-6"
        },
        {
          "city": "武汉",
          "rows": 10000,
          "costMs": 194,
          "thread": "pool-9-thread-7"
        },
        {
          "city": "西安",
          "rows": 10000,
          "costMs": 233,
          "thread": "pool-9-thread-8"
        },
        {
          "city": "南京",
          "rows": 10000,
          "costMs": 209,
          "thread": "pool-9-thread-9"
        },
        {
          "city": "重庆",
          "rows": 10000,
          "costMs": 226,
          "thread": "pool-9-thread-10"
        }
      ],
      "suggestion": "增大线程数可减少总耗时，但受限于数据库连接池和CPU核数"
    }
  },
  {
    "name": "age_shard_t5_L20k",
    "category": "parallel",
    "client_ms": 697.75,
    "success": true,
    "server_data": {
      "description": "多线程按年龄分片并行查询，线程数=5，每片limit=20000",
      "totalCostMs": 670,
      "parallel": true,
      "threadCount": 5,
      "hasIndex": false,
      "totalRows": 100000,
      "taskDetails": [
        {
          "ageRange": "10~21",
          "rows": 20000,
          "costMs": 666,
          "thread": "pool-10-thread-1"
        },
        {
          "ageRange": "22~33",
          "rows": 20000,
          "costMs": 666,
          "thread": "pool-10-thread-2"
        },
        {
          "ageRange": "34~45",
          "rows": 20000,
          "costMs": 662,
          "thread": "pool-10-thread-3"
        },
        {
          "ageRange": "46~57",
          "rows": 20000,
          "costMs": 670,
          "thread": "pool-10-thread-4"
        },
        {
          "ageRange": "58~59",
          "rows": 20000,
          "costMs": 665,
          "thread": "pool-10-thread-5"
        }
      ],
      "suggestion": "与单线程全量查询对比可以看出多线程分治加速效果"
    }
  },
  {
    "name": "age_shard_t10_L20k",
    "category": "parallel",
    "client_ms": 492.75,
    "success": true,
    "server_data": {
      "description": "多线程按年龄分片并行查询，线程数=10，每片limit=20000",
      "totalCostMs": 478,
      "parallel": true,
      "threadCount": 10,
      "hasIndex": false,
      "totalRows": 180000,
      "taskDetails": [
        {
          "ageRange": "10~15",
          "rows": 20000,
          "costMs": 375,
          "thread": "pool-11-thread-1"
        },
        {
          "ageRange": "16~21",
          "rows": 20000,
          "costMs": 444,
          "thread": "pool-11-thread-2"
        },
        {
          "ageRange": "22~27",
          "rows": 20000,
          "costMs": 461,
          "thread": "pool-11-thread-3"
        },
        {
          "ageRange": "28~33",
          "rows": 20000,
          "costMs": 476,
          "thread": "pool-11-thread-4"
        },
        {
          "ageRange": "34~39",
          "rows": 20000,
          "costMs": 349,
          "thread": "pool-11-thread-5"
        },
        {
          "ageRange": "40~45",
          "rows": 20000,
          "costMs": 449,
          "thread": "pool-11-thread-6"
        },
        {
          "ageRange": "46~51",
          "rows": 20000,
          "costMs": 379,
          "thread": "pool-11-thread-7"
        },
        {
          "ageRange": "52~57",
          "rows": 20000,
          "costMs": 443,
          "thread": "pool-11-thread-8"
        },
        {
          "ageRange": "58~63",
          "rows": 20000,
          "costMs": 334,
          "thread": "pool-11-thread-9"
        },
        {
          "ageRange": "64~59",
          "rows": 0,
          "costMs": 3,
          "thread": "pool-11-thread-10"
        }
      ],
      "suggestion": "与单线程全量查询对比可以看出多线程分治加速效果"
    }
  },
  {
    "name": "age_shard_t10_L50k",
    "category": "parallel",
    "client_ms": 2097.58,
    "success": true,
    "server_data": {
      "description": "多线程按年龄分片并行查询，线程数=10，每片limit=50000",
      "totalCostMs": 2076,
      "parallel": true,
      "threadCount": 10,
      "hasIndex": false,
      "totalRows": 450000,
      "taskDetails": [
        {
          "ageRange": "10~15",
          "rows": 50000,
          "costMs": 1894,
          "thread": "pool-12-thread-1"
        },
        {
          "ageRange": "16~21",
          "rows": 50000,
          "costMs": 1897,
          "thread": "pool-12-thread-2"
        },
        {
          "ageRange": "22~27",
          "rows": 50000,
          "costMs": 2051,
          "thread": "pool-12-thread-3"
        },
        {
          "ageRange": "28~33",
          "rows": 50000,
          "costMs": 2075,
          "thread": "pool-12-thread-4"
        },
        {
          "ageRange": "34~39",
          "rows": 50000,
          "costMs": 2061,
          "thread": "pool-12-thread-5"
        },
        {
          "ageRange": "40~45",
          "rows": 50000,
          "costMs": 2058,
          "thread": "pool-12-thread-6"
        },
        {
          "ageRange": "46~51",
          "rows": 50000,
          "costMs": 1917,
          "thread": "pool-12-thread-7"
        },
        {
          "ageRange": "52~57",
          "rows": 50000,
          "costMs": 1893,
          "thread": "pool-12-thread-8"
        },
        {
          "ageRange": "58~63",
          "rows": 50000,
          "costMs": 1917,
          "thread": "pool-12-thread-9"
        },
        {
          "ageRange": "64~59",
          "rows": 0,
          "costMs": 2,
          "thread": "pool-12-thread-10"
        }
      ],
      "suggestion": "与单线程全量查询对比可以看出多线程分治加速效果"
    }
  },
  {
    "name": "age_shard_t20_L20k",
    "category": "parallel",
    "client_ms": 1036.0,
    "success": true,
    "server_data": {
      "description": "多线程按年龄分片并行查询，线程数=20，每片limit=20000",
      "totalCostMs": 1031,
      "parallel": true,
      "threadCount": 20,
      "hasIndex": false,
      "totalRows": 340000,
      "taskDetails": [
        {
          "ageRange": "10~12",
          "rows": 20000,
          "costMs": 643,
          "thread": "pool-13-thread-1"
        },
        {
          "ageRange": "13~15",
          "rows": 20000,
          "costMs": 650,
          "thread": "pool-13-thread-2"
        },
        {
          "ageRange": "16~18",
          "rows": 20000,
          "costMs": 654,
          "thread": "pool-13-thread-3"
        },
        {
          "ageRange": "19~21",
          "rows": 20000,
          "costMs": 1025,
          "thread": "pool-13-thread-4"
        },
        {
          "ageRange": "22~24",
          "rows": 20000,
          "costMs": 979,
          "thread": "pool-13-thread-5"
        },
        {
          "ageRange": "25~27",
          "rows": 20000,
          "costMs": 1007,
          "thread": "pool-13-thread-6"
        },
        {
          "ageRange": "28~30",
          "rows": 20000,
          "costMs": 1012,
          "thread": "pool-13-thread-7"
        },
        {
          "ageRange": "31~33",
          "rows": 20000,
          "costMs": 934,
          "thread": "pool-13-thread-8"
        },
        {
          "ageRange": "34~36",
          "rows": 20000,
          "costMs": 647,
          "thread": "pool-13-thread-9"
        },
        {
          "ageRange": "37~39",
          "rows": 20000,
          "costMs": 650,
          "thread": "pool-13-thread-10"
        },
        {
          "ageRange": "40~42",
          "rows": 20000,
          "costMs": 663,
          "thread": "pool-13-thread-11"
        },
        {
          "ageRange": "43~45",
          "rows": 20000,
          "costMs": 644,
          "thread": "pool-13-thread-12"
        },
        {
          "ageRange": "46~48",
          "rows": 20000,
          "costMs": 1017,
          "thread": "pool-13-thread-13"
        },
        {
          "ageRange": "49~51",
          "rows": 20000,
          "costMs": 661,
          "thread": "pool-13-thread-14"
        },
        {
          "ageRange": "52~54",
          "rows": 20000,
          "costMs": 637,
          "thread": "pool-13-thread-15"
        },
        {
          "ageRange": "55~57",
          "rows": 20000,
          "costMs": 660,
          "thread": "pool-13-thread-16"
        },
        {
          "ageRange": "58~60",
          "rows": 20000,
          "costMs": 991,
          "thread": "pool-13-thread-17"
        },
        {
          "ageRange": "61~63",
          "rows": 0,
          "costMs": 647,
          "thread": "pool-13-thread-18"
        },
        {
          "ageRange": "64~66",
          "rows": 0,
          "costMs": 645,
          "thread": "pool-13-thread-19"
        },
        {
          "ageRange": "67~59",
          "rows": 0,
          "costMs": 646,
          "thread": "pool-13-thread-20"
        }
      ],
      "suggestion": "与单线程全量查询对比可以看出多线程分治加速效果"
    }
  },
  {
    "name": "idx_city_L10k",
    "category": "index_compare",
    "client_ms": 14928.96,
    "success": true,
    "server_data": {
      "无索引": {
        "description": "无索引查询 column=city value=北京",
        "totalCostMs": 131,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": false,
        "totalRows": 10000,
        "taskDetails": null,
        "suggestion": null
      },
      "有索引": {
        "description": "有索引查询 column=city value=北京",
        "totalCostMs": 152,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": true,
        "totalRows": 10000,
        "taskDetails": null,
        "suggestion": "索引提速 0.86 倍"
      }
    }
  },
  {
    "name": "idx_city_L50k",
    "category": "index_compare",
    "client_ms": 16139.49,
    "success": true,
    "server_data": {
      "无索引": {
        "description": "无索引查询 column=city value=北京",
        "totalCostMs": 779,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": false,
        "totalRows": 50000,
        "taskDetails": null,
        "suggestion": null
      },
      "有索引": {
        "description": "有索引查询 column=city value=北京",
        "totalCostMs": 697,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": true,
        "totalRows": 50000,
        "taskDetails": null,
        "suggestion": "索引提速 1.12 倍"
      }
    }
  },
  {
    "name": "idx_city_L100k",
    "category": "index_compare",
    "client_ms": 17714.93,
    "success": true,
    "server_data": {
      "无索引": {
        "description": "无索引查询 column=city value=北京",
        "totalCostMs": 1449,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": false,
        "totalRows": 100000,
        "taskDetails": null,
        "suggestion": null
      },
      "有索引": {
        "description": "有索引查询 column=city value=北京",
        "totalCostMs": 1582,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": true,
        "totalRows": 100000,
        "taskDetails": null,
        "suggestion": "索引提速 0.92 倍"
      }
    }
  },
  {
    "name": "idx_age_L10k",
    "category": "index_compare",
    "client_ms": 11939.69,
    "success": true,
    "server_data": {
      "无索引": {
        "description": "无索引查询 column=age value=25",
        "totalCostMs": 145,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": false,
        "totalRows": 10000,
        "taskDetails": null,
        "suggestion": null
      },
      "有索引": {
        "description": "有索引查询 column=age value=25",
        "totalCostMs": 352,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": true,
        "totalRows": 10000,
        "taskDetails": null,
        "suggestion": "索引提速 0.41 倍"
      }
    }
  },
  {
    "name": "idx_age_L100k",
    "category": "index_compare",
    "client_ms": 16840.31,
    "success": true,
    "server_data": {
      "无索引": {
        "description": "无索引查询 column=age value=25",
        "totalCostMs": 1291,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": false,
        "totalRows": 100000,
        "taskDetails": null,
        "suggestion": null
      },
      "有索引": {
        "description": "有索引查询 column=age value=25",
        "totalCostMs": 4141,
        "parallel": false,
        "threadCount": 0,
        "hasIndex": true,
        "totalRows": 100000,
        "taskDetails": null,
        "suggestion": "索引提速 0.31 倍"
      }
    }
  },
  {
    "name": "concurrent_5",
    "category": "concurrent",
    "client_ms": 36.38,
    "success": true,
    "server_data": {
      "concurrency": 5,
      "success_count": 5,
      "avg_ms": 33.41,
      "max_ms": 34.36,
      "min_ms": 32.42,
      "total_ms": 36.38
    }
  },
  {
    "name": "concurrent_10",
    "category": "concurrent",
    "client_ms": 32.19,
    "success": true,
    "server_data": {
      "concurrency": 10,
      "success_count": 10,
      "avg_ms": 23.11,
      "max_ms": 28.06,
      "min_ms": 18.94,
      "total_ms": 32.19
    }
  },
  {
    "name": "concurrent_20",
    "category": "concurrent",
    "client_ms": 53.95,
    "success": true,
    "server_data": {
      "concurrency": 20,
      "success_count": 20,
      "avg_ms": 31.02,
      "max_ms": 39.03,
      "min_ms": 22.67,
      "total_ms": 53.95
    }
  },
  {
    "name": "concurrent_50",
    "category": "concurrent",
    "client_ms": 128.95,
    "success": true,
    "server_data": {
      "concurrency": 50,
      "success_count": 50,
      "avg_ms": 51.82,
      "max_ms": 83.16,
      "min_ms": 24.01,
      "total_ms": 128.95
    }
  }
]
```
</details>
