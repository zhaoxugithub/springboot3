package com.atguigu.boot3.redis;

import org.redisson.Redisson;
import org.redisson.api.*;
import org.redisson.api.listener.ScoredSortedSetAddListener;
import org.redisson.config.Config;

import java.util.*;

public class RScoredSortedSetDemo {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://150.158.27.19:6379")
                .setPassword("NFTurbo666");
        RedissonClient redisson = Redisson.create(config);
        try {
            RScoredSortedSet<String> rankedSet = redisson.getScoredSortedSet("demo_set");
            // 添加元素
            rankedSet.add(95.5, "Alice");
            rankedSet.add(88.0, "Bob");
            rankedSet.add(92.5, "Charlie");
            // 查询Top 2（降序）
            Collection<String> top2 = rankedSet.valueRangeReversed(0, 1);
            System.out.println("Top 2: " + top2);
            // 添加监听器（异步处理）
            int listenerId = rankedSet.addListener((ScoredSortedSetAddListener) value -> {
                System.out.println("New entry added: " + value);
            });
            // 模拟新数据插入
            rankedSet.add(98.0, "David");
            // 查询Top 2（降序）
            Collection<String> newTop2 = rankedSet.valueRangeReversed(0, 1);
            System.out.println("Top 2: " + newTop2);
            // 等待一下让监听器有时间触发
            Thread.sleep(1000);
            // 移除监听器
            rankedSet.removeListener(listenerId);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrupted: " + e.getMessage());
        } finally {
            redisson.shutdown();
        }
    }
}