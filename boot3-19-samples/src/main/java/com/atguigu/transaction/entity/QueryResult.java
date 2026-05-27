package com.atguigu.transaction.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryResult {

    /** 查询描述 */
    private String description;

    /** 总耗时（毫秒） */
    private long totalCostMs;

    /** 是否使用并行 */
    private boolean parallel;

    /** 线程数（并行时有效） */
    private int threadCount;

    /** 是否有索引 */
    private boolean hasIndex;

    /** 查询到的总条数 */
    private long totalRows;

    /** 每个子任务耗时明细（并行时展示） */
    private List<Map<String, Object>> taskDetails;

    /** 建议 */
    private String suggestion;
}

