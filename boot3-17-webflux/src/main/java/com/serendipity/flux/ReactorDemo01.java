package com.serendipity.flux;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

/**
 * ClassName ReactorDemo01
 * Description TODO
 * Author 11931
 * Date 2023-08-14:4:30
 * Version 1.0
 **/
@Slf4j
public class ReactorDemo01 {

    @Test
    public void test01() {
        // Flux 实现了Publish
        Flux.just("Ben", "Michael", "Mark")
            // Subscriber 是订阅者/观察者
            .subscribe(new Subscriber<String>() {
                @Override
                public void onSubscribe(Subscription s) {
                    // 需要两个元素
                    s.request(2);
                }

                @Override
                public void onNext(String s) {
                    System.out.println("Hello " + s + "!");
                }

                // OnError 和 onComplete 性质上是一样的
                // 都是用来终止操作
                @Override
                public void onError(Throwable t) {
                    System.out.println(t.getMessage());
                }

                @Override
                public void onComplete() {
                    System.out.println("Completed");
                }
            });
    }


    // 是test01的优化
    @Test
    public void test02() {
        Flux.just("Ben", "Michael", "Mark")
            .limitRequest(2) //  s.request(2);
            .doOnNext(s -> System.out.println("Hello " + s + "!"))
            // 在数据消费完之后执行
            .doOnComplete(() -> System.out.println("Completed"))
            // 在消费数据有异常的时候执行
            .doOnError(throwable -> System.out.println(throwable.getMessage()))
            .subscribe();
    }


    @Test
    public void test03() {
        Flux.range(1, 10000)
            .takeWhile(i -> i < 10)
            .subscribe(System.out::println);
    }

    @Test
    public void test04() {
        Flux.just(1, 2)
            .concatWith(Mono.error(new IllegalAccessError()))
            .subscribe(System.out::println, System.err::println);
    }

    @Test
    public void test05() {
        Flux.just(1, 2)
            .concatWith(Mono.error(new IllegalAccessError()))
            // 在数据有异常的时候返回
            .onErrorReturn(0)
            .subscribe(System.out::println);

        // Flux.just(1, 2)
        //     .concatWith(Mono.error(new IllegalStateException()))
        //     .onErrorResume(e -> {
        //         if (e instanceof IllegalStateException) {
        //             return Mono.just(0);
        //         } else if (e instanceof IllegalArgumentException) {
        //             return Mono.just(0);
        //         }
        //         return Mono.empty();
        //     })
        //     .subscribe(System.out::println);
    }

    @Test
    public void test05_1() {
        // Flux.range(1, 5)
        //     .map(i -> i == 2 ? i / 0 : i)
        //     .subscribe(System.out::println);

        // Flux.range(1, 5)
        //     .map(i -> i == 2 ? i / 0 : 1)
        //     // 异常捕获,不会打印日志,有异常直接返回,并且后面的数据也不进行消费了
        //     .onErrorReturn(0)
        //     .subscribe(System.out::println);

        // Flux.range(1, 5)
        //     .map(i -> i == 2 ? i / 0 : 1)
        //     // 异常捕获,有错误,后面也不会继续消费了,自定义返回
        //     .onErrorResume(err -> {
        //         // 打印错误日志
        //         System.out.println(err.getMessage());
        //         return Mono.just(-1);
        //     })
        //     .subscribe(System.out::println);

        // Flux.range(1, 5)
        //     .map(i -> i == 2 ? i / 0 : 1)
        //     // 异常捕获,后面不继续消费了
        //     .doOnError(err -> {
        //         System.out.println(err.getMessage());
        //         // 无返回值
        //     })
        //     .subscribe(System.out::println);

        Flux.range(1, 5)
            .map(i -> i == 2 ? i / 0 : 1)
            .onErrorMap(err -> {
                System.out.println(err.getMessage());
                // 这里强制返回err
                return err;
            })
            .subscribe(System.out::println);
    }

    @Test
    public void test05_2() {
        // Flux.range(1, 5)
        //     .map(i -> i == 2 ? i / 0 : 1)
        //     // 异常捕获,有错误,后面也不会继续消费了,自定义返回
        //     .onErrorResume(err -> {
        //         // 打印错误日志
        //         System.out.println(err.getMessage());
        //         return Mono.just(-1);
        //     })
        //     .subscribe(System.out::println);


        Flux.range(1, 5)
            .flatMap(i -> {
                Mono.just(i)
                    .map(k -> k == 2 ? k / 0 : 1)
                    .onErrorResume(err -> {
                        System.out.println(err.getMessage());
                        return Mono.empty();
                    });
                return Mono.just(i);
            })
            .subscribe(System.out::println);
    }

    @Test
    public void test06() {
        Flux.just(1, 2)

            // 在数据流的后面加上这个异常
            .concatWith(Mono.error(new IllegalStateException()))
            .retry(1)
            .subscribe(System.out::println);
    }

    @Test
    public void test07() {
        Flux.just(1, 0)
            .map(x -> 1 / x)
            .checkpoint("test")
            .subscribe(System.out::println);
    }

    @Test
    public void test08() {
        Flux.range(1, 2)
            .log("Range")
            .subscribe(System.out::println);
    }

    @Test
    public void test09() throws InterruptedException {
        Flux<Long> source = Flux.interval(Duration.ofMillis(1000))
                                .take(10)
                                .publish()
                                .autoConnect();

        source.subscribe();
        Thread.sleep(5000);
        source.toStream()
              .forEach(System.out::println);
    }

    Function<Long, Long> log() {
        return aLong -> {
            log.info("num is {}", aLong);
            return aLong;
        };
    }

    Function<Long, Flux<Long>> longOfFlux() {
        return aLong -> {
            log.info("num is {}", aLong);
            return Flux.just(aLong);
        };
    }

    @Test
    public void test10() {
        Flux<Long> longFlux = Flux.interval(Duration.of(100, ChronoUnit.MILLIS))
                                  // 在当前线程执行
                                  .map(log())
                                  //// 在当前线程执行
                                  .flatMap(longOfFlux());
        longFlux.subscribe();
    }

    @Test
    public void test11() throws InterruptedException {
        Mono<Date> m1 = Mono.just(new Date());
        // 懒创建
        Mono<Date> m2 = Mono.defer(() -> Mono.just(new Date()));
        m1.subscribe(System.out::println);
        m2.subscribe(System.out::println);

        Thread.sleep(5000);

        m1.subscribe(System.out::println);
        m2.subscribe(System.out::println);
        /*
        Mon Aug 14 19:05:26 CST 2023
        Mon Aug 14 19:05:26 CST 2023
        Mon Aug 14 19:05:26 CST 2023
        Mon Aug 14 19:05:31 CST 2023
         */
    }
}
