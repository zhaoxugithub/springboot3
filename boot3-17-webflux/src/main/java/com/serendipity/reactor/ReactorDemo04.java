package com.serendipity.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;

/**
 * ClassName ReactorDemo04
 * Description TODO
 * Author 11931
 * Date 2023-08-29:18:31
 * Version 1.0
 **/
public class ReactorDemo04 {
    // 组合响应流
    @Test
    public void test01() throws InterruptedException {
        // concat
        // 流是按照顺序订阅,按顺序处理
        Flux.concat(Flux.range(10, 5)
                        .delayElements(Duration.ofMillis(100))
                        .doOnSubscribe(subscription -> System.out.println("第一个订阅")), Flux.range(100, 5)
                                                                                              .delayElements(Duration.ofMillis(100))
                                                                                              .doOnSubscribe(subscription -> System.out.println("第二个订阅")))
            .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }

    @Test
    public void test02() throws InterruptedException {
        // merge
        // 上游流和下游流是同事被订阅
        Flux.merge(Flux.range(10, 5)
                       .delayElements(Duration.ofMillis(100))
                       .doOnSubscribe(subscription -> System.out.println("第一个订阅")), Flux.range(100, 5)
                                                                                             .delayElements(Duration.ofMillis(100))
                                                                                             .doOnSubscribe(subscription -> System.out.println("第二个订阅")))
            .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }

    @Test
    public void test03() throws InterruptedException {
        // zip,两个流拼接成组
        // 这里面依照最慢的那个进行打印,如果我们两个流的打印个数不同,那就以最少得为准
        Flux.zip(Flux.range(1, 10)
                     .delayElements(Duration.ofMillis(100)), Flux.range(100, 5)
                                                                 .delayElements(Duration.ofMillis(10)))
            .subscribe(System.out::println);
        Thread.sleep(1000 * 10);
    }

    @Test
    public void test04() throws InterruptedException {
        // combineLatest
        Flux.zip(Flux.range(1, 20)
                     .delayElements(Duration.ofMillis(1000)), Flux.range(100, 10)
                                                                  .delayElements(Duration.ofMillis(2000)), ((integer1, integer2) -> integer1 + "==" + integer2))
            .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }
}



