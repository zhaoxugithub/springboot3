package com.serendipity.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * ClassName ReactorDemo02
 * Description TODO
 * Author 11931
 * Date 2023-08-29:2:02
 * Version 1.0
 **/
public class ReactorDemo02 {
    @Test
    public void test01() {
/*        Flux.range(1, 10)
            .map(item -> "hello " + item)
            .subscribe(System.out::println);
        // .subscribe(System.out::println);*/
        Flux.range(0, 10)
                .map(item -> "hello everyone " + item)
                .index()
                .subscribe(item -> {
                    // 只有调用了index()这个方法才能够item.getT1()
                    Long t1 = item.getT1();
                    String t2 = item.getT2();
                    System.out.println("t1=" + t1 + ",t2=" + t2);
                }, System.err::println, () -> System.out.println("over"));
        Flux.range(0, 10)
                .map(item -> "hello everyone " + item)
                .timestamp()
                .subscribe(item -> {
                    // 只有调用了index()这个方法才能够item.getT1()
                    Long t1 = item.getT1();
                    String t2 = item.getT2();
                    System.out.println("t1=" + t1 + ",t2=" + t2);
                }, System.err::println, () -> System.out.println("over"));
    }


    @Test
    public void test02() throws InterruptedException {
        // 这里在产生第一个元素之前就会停顿1s
        Flux.interval(Duration.ofMillis(1000))
                .map(item -> "hello " + item)
                .doOnNext(System.out::println)
                // 在第二条流来之后才能开始订阅
                .skipUntilOther(Mono.just("start")
                        .delayElement(Duration.ofSeconds(3)))
                // 在第三条流来之后就停止订阅
                .takeUntilOther(Mono.just("end")
                        .delayElement(Duration.ofSeconds(6)))
                .subscribe(item -> System.out.println("onNext: " + item), ex -> System.err.println("onError: " + ex), () -> System.out.println("onComplete"));

        Thread.sleep(1000 * 10);
    }

    @Test
    public void test03() {
        /*
        Flux.just(1, 2, 36, 4, 25, 6, 7)
            // 默认升序小->大
            // reverseOrder 大->小
            .collectSortedList(Comparator.reverseOrder())
            .subscribe(System.out::println);
        Flux.just(1, 2, 3, 4, 5, 6)
            // 这里是操作的map的key -> key1,key2,key3.....
            .collectMap(item -> "key" + item)
            .subscribe(System.out::println); // 打印出来时无序的
        //  另外一种方式
        Flux.just(1, 2, 3, 4, 5, 6)
            // key 和value分开
            .collectMap(key1 -> "key" + key1, value -> value)
            .subscribe(System.out::println);
        Flux.just(1, 2, 3, 4, 5, 6)
            .collectMap(k -> "key" + k, v -> v, () -> {
                // 追加map元素
                HashMap<Object, Object> map = new HashMap<>();
                for (int i = 7; i < 10; i++) {
                    map.put("key:" + i, "value:" + i);
                }
                return map;
            })
            .subscribe(System.out::println);
        // collectMultimap()
        Flux.just(1, 2, 3, 4, 5)
            .collectMultimap(k -> "key" + k, v -> {
                ArrayList<String> values = new ArrayList<>();
                for (int i = 0; i < v; i++) {
                    values.add("value" + i);
                }
                return values;
            })
            .subscribe(System.out::println);
        Flux.just(1, 2, 3, 4, 5)
            .collectMultimap(k -> "key" + k, v -> {
                        ArrayList<String> values = new ArrayList<>();
                        for (int i = 0; i < v; i++) {
                            values.add("value" + i);
                        }
                        return values;
                    },
                    // 扩充元素
                    () -> {
                        return getMap();
                    })
            .subscribe(System.out::println);
          */
    }

    public Map<String, List<String>> getMap() {
        Map<String, List<String>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            list.clear();
            for (int j = 0; j < i; j++) {
                list.add("ele" + j);
            }
            map.put(i + "key", list);
        }
        return map;
    }

    @Test
    public void test04() {
        // repeat
        Flux.just(1, 2, 3)
                // 实际上打印了4次,3次拷贝
                .repeat(3)
                .subscribe(System.out::println);
    }

    @Test
    public void test05() {
        Flux.empty()
                // 如果返回空的就赋值默认值
                .defaultIfEmpty("hello")
                .subscribe(System.out::println);
    }

    @Test
    public void test06() {
        // distinct 去重
        Flux.just(1, 2, 3, 4)
                .repeat(3)
                .distinct()
                .subscribe(System.out::println);
    }

    @Test
    public void test07() {
        // distinctUntilChanged
        Flux.just(1, 1, 1, 1, 2, 2, 2, 3, 3, 3, 3, 4, 1, 1, 1, 2, 2, 2)
                // 局部去重
                .distinctUntilChanged()
                .subscribe(System.out::print);
    }
}
