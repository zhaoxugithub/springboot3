package com.serendipity.controller;

import org.reactivestreams.Publisher;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassName FluxTest
 * Description TODO
 * Author 11931
 * Date 2022-12-04:2:12
 * Version 1.0
 **/
public class FluxTest {
    public static void fluxTest() {
        /*
        just()：可以指定序列中包含的全部元素。创建出来的 Flux 序列在发布这些元素之后会自动结束。
        fromArray()：可以从一个数组、Iterable 对象或 Stream 对象中创建 Flux 对象。
        empty()：创建一个不包含任何元素，只发布结束消息的序列。
        range(int start, int count)：创建包含从 start 起始的 count 个数量的 Integer 对象的序列。
        interval(Duration period)和 interval(Duration delay, Duration period)：创建一个包含了从 0 开始递增的 Long 对象的序列。
        其中包含的元素按照指定的间隔来发布。除了间隔时间之外，还可以指定起始元素发布之前的延迟时间。
         */
        Flux.just("Hello", "World")
            .subscribe(System.out::println);
        Flux.fromArray(new Integer[]{1, 2, 3})
            .subscribe(System.out::println);
        Flux.empty()
            .subscribe(System.out::println);
        Flux.range(1, 10)
            .subscribe(System.out::println);
        Flux.interval(Duration.of(10, ChronoUnit.SECONDS))
            .subscribe(System.out::println);
    }

    public void test1() {
        WebClient webClient = WebClient.builder()
                                       .baseUrl("http://jsonplaceholder.typicode.com")
                                       .build();

        Mono<String> mono = webClient.get()  // GET 请求
                                     .uri("/postss/1")  // 请求路径,注意为了制造异常，这里是错的
                                     .retrieve()  // 获取请求结果
                                     .bodyToMono(String.class)  // 用Mono接收单个非集合对象数据
                                     .doOnError(Exception.class, err -> {  // 处理异常
                                         System.out.println(LocalDateTime.now() + "---发生错误：" + err.getMessage());
                                     })
                                     .retry(3);
    }

    public int[] twoSum(int[] nums, int target) {
        if (null == nums || nums.length == 0) {
            return new int[]{-1, -1};
        }
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < nums.length; i++) {
            int another = target - nums[i];
            if (map.containsKey(nums[another])) {
                return new int[]{i, map.get(another)};
            } else {
                map.put(nums[i], i);
            }
        }
        return new int[]{-1, -1};
    }

    public static void main(String[] args) {
        fluxTest();
        Map<String, String> map = new HashMap<>();
    }
}
