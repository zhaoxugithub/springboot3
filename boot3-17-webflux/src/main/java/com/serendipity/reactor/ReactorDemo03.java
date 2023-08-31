package com.serendipity.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.Transient;
import java.util.Arrays;

/**
 * ClassName ReactorDemo03
 * Description TODO
 * Author 11931
 * Date 2023-08-29:15:28
 * Version 1.0
 **/
public class ReactorDemo03 {
    @Test
    public void test01() {
        Flux.just(1, 2, 3, 4, 5, 6)
                // 执行到2就不执行了,遇到第一个偶数就不执行了
                .any(item -> item % 2 == 0)
                .subscribe(System.out::println);

        Flux.just(1, 2, 3, 4, 5, 6)
                .doOnNext(System.out::println)
                .any(item -> item % 2 == 0)
                .subscribe(System.out::println);
    }

    @Test
    public void test02() {
        // 1,2,3,4,5
        // reduce
        Flux.range(1, 5)
                .reduce(0, (num1, num2) -> {
                    System.out.println("num1:" + num1);
                    System.out.println("num2:" + num2);
                    return num1 + num2;
                })
                .subscribe(System.out::println);
    }

    @Test
    public void test03() {
        // scan 可以打印中间值
        Flux.range(1, 5)
                .scan(0, (num1, num2) -> {
                    System.out.println("num1:" + num1);
                    System.out.println("num2:" + num2);
                    return num1 + num2;
                })
                .subscribe(System.out::println);
    }

    @Test
    public void test04() {
        int arLength = 5;
        Flux.range(1, 500)
                .index()
                .scan(new int[arLength], (arr, entry) -> {
                    arr[(int) (entry.getT1() % arLength)] = entry.getT2();
                    return arr;
                })
                .skip(arLength)
                .map(array -> Arrays.stream(array)
                        .sum() * 1.0 / array.length)
                .subscribe(System.out::println);
    }

    @Test
    public void test05() {
        // then
        // Mono 和 Flux 流有 then、thenMany 和 thenEmpty 操作符，它们在上游流完成时完成。
        // 上游流完成处理后，这些操作符可用于触发新流，订阅是对于新流的。
        Flux.just(1, 2, 3, 4)
                .doOnNext(item -> System.out.println("第一条流:" + item))
                .thenMany(Flux.just(5, 6, 7))
                .subscribe(System.out::println);
        Flux.just(1, 2, 3, 4)
                .doOnNext(item -> System.out.println("第一条流:" + item))
                // 组合第二条流
                .then(Mono.just(5))
                // 实际上消费的是Mono.just(5)
                .subscribe(System.out::println);
        Flux.just(1, 2, 3, 4)
                .doOnNext(item -> System.out.println("item:" + item))
                .thenEmpty(Mono.empty())
                .subscribe();
    }

    @Test
    public void test06() {
    }
}
