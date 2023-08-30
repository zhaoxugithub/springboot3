package com.serendipity.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;

/**
 * ClassName ReactorDemo05
 * Description TODO
 * Author 11931
 * Date 2023-08-29:23:14
 * Version 1.0
 **/
public class ReactorDemo05 {
    /*
    1.do0nSubscribe是事件被订阅之前(也就是事件源发起之前)会调用的方法，它一般执行在 subscribe) 发生的线程，而如果在
        doOnSubscribe()之后有 subscribeOn() 的话，它将执行在离它最近的 subscribeOn) 所指定的线程
    2.doOnNext是观察者被通知之前(也就是回调之前)会调用的方法，说白了就是最终回调之前的前一个回调方法，这个方法一般做的事
        件类似于观察者做的事情，只是自己不是最终的回调者。 (观察者即最终回调者)
     */
    @Test
    public void test01() {
        Flux.just(Arrays.asList(1, 2, 3), Arrays.asList("a", "b", "c", "d"), Arrays.asList(7, 8, 9))
            .doOnNext(System.out::println)
            .flatMap(item -> Flux.fromIterable(item)
                                 .doOnSubscribe(subscription -> {
                                     System.out.println(subscription);
                                     System.out.println("已经订阅");
                                 }))
            .subscribe(System.out::println);
    }

    @Test
    public void test02() throws InterruptedException {
        Random random = new Random();
        Flux.just(Arrays.asList(1, 2, 3), Arrays.asList("a", "b", "c", "d"), Arrays.asList(7, 8, 9))
            .doOnNext(System.out::println)
            .flatMap(item -> Flux.fromIterable(item)
                                 .doOnSubscribe(subscription -> {
                                     System.out.println(subscription);
                                     System.out.println("已经订阅");
                                 }))
            .delayElements(Duration.ofMillis(random.nextInt(100) + 500))
            .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }

    @Test
    public void test03() throws InterruptedException {
        // concatMap
        Random random = new Random();
        Flux.just(Arrays.asList(1, 2, 3), Arrays.asList("a", "b", "c", "d"), Arrays.asList(7, 8, 9))
            .doOnNext(System.out::println)
            .concatMap(item -> Flux.fromIterable(item)
                                   .doOnSubscribe(subscription -> {
                                       System.out.println(subscription);
                                       System.out.println("已经订阅");
                                   }))
            .delayElements(Duration.ofMillis(random.nextInt(100) + 100))
            .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }

    @Test
    public void test04() throws InterruptedException {
        // flapMapSequential //内部排序输出
        Random random = new Random();
        Flux.just(Arrays.asList(1, 2, 3), Arrays.asList("a", "b", "c", "d"), Arrays.asList(7, 8, 9))
            .doOnNext(System.out::println)
            .flatMapSequential(item -> Flux.fromIterable(item)
                                   .doOnSubscribe(subscription -> {
                                       System.out.println(subscription);
                                       System.out.println("已经订阅");
                                   }))
            .delayElements(Duration.ofMillis(random.nextInt(100) + 100))
            .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }
}
