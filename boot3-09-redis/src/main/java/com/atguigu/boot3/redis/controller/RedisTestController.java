package com.atguigu.boot3.redis.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import com.atguigu.boot3.redis.entity.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author lfy
 * @Description
 * @create 2023-04-28 15:43
 */
@RestController
public class RedisTestController {

    @Autowired
    StringRedisTemplate stringRedisTemplate;


    // 为了后来系统的兼容性，应该所有对象都是以json的方式进行保存
    @Autowired // 如果给redis中保存数据会使用默认的序列化机制，导致redis中保存的对象不可视
    private RedisTemplate<Object, Object> redisTemplate;

    @GetMapping("/count")
    public String count() {
        Long hello = stringRedisTemplate.opsForValue()
                .increment("hello", 1);
        // 常见数据类型  k: v value可以有很多类型
        // string： 普通字符串 ： redisTemplate.opsForValue()
        // list:    列表：       redisTemplate.opsForList()
        // set:     集合:       redisTemplate.opsForSet()
        // zset:    有序集合:    redisTemplate.opsForZSet()
        // hash：   map结构：    redisTemplate.opsForHash()
        return "访问了【" + hello + "】次";
    }

    @GetMapping("execCommonApi")
    public String execCommonApi() {
        return stringCommonAPI();
    }


    @GetMapping("execHashCommonAPI")
    public Map<String, String> execHashCommonApi() {
        return hashCommonAPI();
    }


    @GetMapping("execListCommonAPI")
    public Map<String, String> execListCommonApi() {
        return listCommonAPI();
    }


    @GetMapping("execSetCommonAPI")
    public Map<String, String> execSetCommonAPI() {
        SetOperations<String, String> setOperations = stringRedisTemplate.opsForSet();
        setOperations.add("tags");
        setOperations.isMember("tags", "Java");
        Set<String> tags = setOperations.members("tags");
        // 假设已经注入 RedisSetService
        setOperations.add("tags", "Java", "Redis", "Spring", "Redis"); // 重复元素会被去重
        // 判断元素是否存在
        Boolean hasJava = setOperations.isMember("tags", "Java"); // true
        // 获取集合所有元素
        Set<String> allTags = setOperations.members("tags"); // [Java, Redis, Spring]
        // 随机抽奖
        String lucky = setOperations.randomMember("lottery");
        // 集合运算：两个集合的交集
        Set<String> common = setOperations.intersect("set1", "set2");
        return null;
    }


    public Map<String, String> listCommonAPI() {
        ListOperations<String, String> listOperation = stringRedisTemplate.opsForList();

        listOperation.leftPush("mylist", "v1");
        listOperation.leftPush("mylist", "v2");
        listOperation.leftPush("mylist", "v3");
        listOperation.rightPush("mylist", "v4");
        // 替换了
        listOperation.set("mylist", 2, "sssss");


        listOperation.leftPush("mylist2", "1");
        listOperation.leftPush("mylist2", "2");
        Map<String, String> map = new HashMap<>();

        stringRedisTemplate.keys("mylist*").forEach(key -> {
            map.put(key, JSONUtil.toJsonPrettyStr(listOperation.range(key, 0, -1)));
        });
        return map;
    }


    private String stringCommonAPI() {
        // 列举一下redis 常见的API操作案例
        // 1 min 过期
        stringRedisTemplate.opsForValue().set("hello", "world", 1, TimeUnit.MINUTES);
        Long increment = stringRedisTemplate.opsForValue().increment("page:view", 1);
        // 设置过个键值对
        stringRedisTemplate.opsForValue().multiSet(Map.of("k1", "v1", "k2", "v2"));
        // 查询多个key 值
        Objects.requireNonNull(stringRedisTemplate.opsForValue().multiGet(
                List.of("k1", "k2"))).forEach(System.out::println);
        // 仅当键不存在时设置，常用于分布式锁
        stringRedisTemplate.opsForValue().setIfAbsent("key1", "kkk");
        String oldValue = stringRedisTemplate.opsForValue().getAndSet("key1", "new kkkk");
        System.out.println(oldValue);
        return "commonAPI execute";
    }


    private Map<String, String> hashCommonAPI() {

        HashMap<String, String> map = MapUtil.newHashMap();

        stringRedisTemplate.opsForHash().put("user:01", "name", "zhangsan");
        stringRedisTemplate.opsForHash().put("user:01", "age", "20");
        stringRedisTemplate.opsForHash().put("user:02", "name", "wangwu");
        stringRedisTemplate.opsForHash().put("work:01", "name", "w1");

        // 获取所有的key, 致命缺陷：Redis 是单线程，keys 命令会全量扫描所有 Key，数据量大时会导致线上服务卡顿。
        stringRedisTemplate.keys("*").forEach(System.out::println);
        // 获取user:* key
        Set<String> keys = stringRedisTemplate.keys("user:*");

        keys.forEach(key -> {
            Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
            map.put(key, JSONUtil.toJsonPrettyStr(entries));
        });

        // 1. 扫描所有以 "user:" 开头的 Key（每次只拿10条，不阻塞）
        ScanOptions options = ScanOptions.scanOptions().match("user:*").count(10).build();
        Cursor<byte[]> cursor = stringRedisTemplate.getConnectionFactory()
                .getConnection()
                .scan(options);
        while (cursor.hasNext()) {
            String foundKey = new String(cursor.next(), StandardCharsets.UTF_8);
            // 2. 找到 Key 后，再去拿这个哈希表的所有数据
            Map<Object, Object> userData = stringRedisTemplate.opsForHash().entries(foundKey);
            map.put(foundKey, JSONUtil.toJsonPrettyStr(userData));
        }
        cursor.close();

        return map;
    }


    @GetMapping("/person/save")
    public String savePerson() {
        Person person = new Person(1L, "张三", 18, new Date());
        // 1、序列化： 对象转为字符串方式
        redisTemplate.opsForValue()
                .set("person", person);
        return "ok";
    }

    @GetMapping("/person/get")
    public Person getPerson() {
        Person person = (Person) redisTemplate.opsForValue()
                .get("person");
        return person;
    }

    public void test() {
        ValueOperations<Object, Object> stringOps = redisTemplate.opsForValue();
        ListOperations<Object, Object> listOps = redisTemplate.opsForList();
        SetOperations<Object, Object> setOps = redisTemplate.opsForSet();
        ZSetOperations<Object, Object> objectObjectZSetOperations = redisTemplate.opsForZSet();
    }
}
