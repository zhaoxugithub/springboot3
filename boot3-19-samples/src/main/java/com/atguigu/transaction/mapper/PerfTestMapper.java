package com.atguigu.transaction.mapper;

import com.atguigu.transaction.entity.PerfTest;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface PerfTestMapper extends BaseMapper<PerfTest> {

    /** 按年龄范围查询 */
    @Select("SELECT * FROM perf_test WHERE age BETWEEN #{minAge} AND #{maxAge} LIMIT #{limit}")
    List<PerfTest> selectByAgeRange(@Param("minAge") int minAge,
                                    @Param("maxAge") int maxAge,
                                    @Param("limit") int limit);

    /** 按城市查询 */
    @Select("SELECT * FROM perf_test WHERE city = #{city} LIMIT #{limit}")
    List<PerfTest> selectByCity(@Param("city") String city, @Param("limit") int limit);

    /** 按姓名模糊查询 */
    @Select("SELECT * FROM perf_test WHERE name LIKE CONCAT('%', #{keyword}, '%') LIMIT #{limit}")
    List<PerfTest> selectByNameLike(@Param("keyword") String keyword, @Param("limit") int limit);

    /** 查询总数 */
    @Select("SELECT COUNT(*) FROM perf_test")
    long countAll();

    /** 添加索引 */
    @Update("CREATE INDEX ${indexName} ON perf_test(${column})")
    void createIndex(@Param("indexName") String indexName, @Param("column") String column);

    /** 删除索引 */
    @Update("DROP INDEX ${indexName} ON perf_test")
    void dropIndex(@Param("indexName") String indexName);

    /** 调用存储过程插入100万条数据 */
    @Update("CALL insert_perf_test_data()")
    void callInsertProcedure();

    /** 清空表 */
    @Update("TRUNCATE TABLE perf_test")
    void truncateTable();
}

