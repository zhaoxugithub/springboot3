package com.serendipity.reactor;

import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Optional;

/**
 * ClassName ReactorDemo01
 * Description TODO
 * Author 11931
 * Date 2023-08-29:0:31
 * Version 1.0
 **/
public class ReactorDemo01 {
    @Test
    public void test01() {
        Flux<String> just = Flux.just("hello", "everybody");
        just.subscribe(System.out::println);
        Flux<String> stringFlux = Flux.fromArray(new String[]{"1", "2", "3"});
        stringFlux.subscribe(System.out::println);
        Flux<Integer> integerFlux = Flux.fromIterable(Arrays.asList(1, 2, 3, 4, 5));
        integerFlux.subscribe(System.out::println);
        Flux<Integer> range = Flux.range(1000, 5);
        range.subscribe(System.out::println);
    }

    @Test
    public void test02() {
        Mono<String> hello = Mono.just("hello");
        hello.subscribe(System.out::println);
        Mono<Object> empty = Mono.empty();
        empty.subscribe(System.out::println);
        Mono<Object> objectMono = Mono.justOrEmpty(Optional.empty());
        objectMono.subscribe(System.out::println);
    }

    @Test
    public void test03() {
        // Flux 和 Mono 使用from(Publish<T> p) 工厂方法
        Flux.from(new Publisher<Object>() {
                    @Override
                    public void subscribe(Subscriber<? super Object> s) {
                        for (int i = 0; i < 10; i++) {
                            s.onNext("hello" + i);
                        }
                        s.onComplete();
                    }
                })
                .subscribe(System.out::println, System.err::println, () -> System.out.println("处理结束"));
    }

    @Test
    public void test04() {
        // defer 工厂创建序列(在订阅式决定其行为)
        class A {
            boolean isValidValue(String value) {
                System.out.println("调用了 isValidValue的方法");
                return true;
            }

            String getData(String value) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                return "echo:" + value;
            }

            Mono<String> requestData(String value) {
                return isValidValue(value) ? Mono.fromCallable(() -> getData(value)) : Mono.error(new RuntimeException("isValid value"));
            }

            Mono<String> requestMonoData(String value) {
                return Mono.defer(() -> isValidValue(value) ? Mono.fromCallable(() -> getData(value)) : Mono.error(new RuntimeException()));
            }
        }
        A a = new A();
        // 这个不需要subscribe 就会执行isValidValue
        a.requestData("zs");
        // Mono.defer 除非subscribe才会执行isValidValue
        a.requestMonoData("sa");
        a.requestMonoData("da")
                .subscribe();
    }

    @Test
    public void test05() {
        // // 订阅式响应流
        // Flux.range(100, 10)
        //     .filter(num -> num % 3 == 0)
        //     .map(num -> "hello msb " + num)
        //     .doOnNext(System.out::println)
        //     .subscribe();
        //
        // // 添加加订阅者
        // Flux.range(100, 10)
        //     .filter(num -> num % 2 == 0)
        //     .subscribe(System.out::println);
        //
        // // 添加对异常的处理
        // Flux.from(subscriber -> {
        //         // subscirbe的具体执行方法
        //         for (int i = 0; i < 5; i++) {
        //             // 发布元素
        //             subscriber.onNext(i);
        //         }
        //         subscriber.onError(new Exception("测试数据异常"));
        //     })
        //     .subscribe(num -> System.out.println(num), ex -> System.out.println("异常梳理" + ex));
        //
        //
        // // 完成事件的处理
        //
        // Flux.from(new Publisher<Integer>() {
        //         @Override
        //         public void subscribe(Subscriber<? super Integer> s) {
        //             for (int i = 0; i < 10; i++) {
        //                 s.onNext(i);
        //             }
        //             s.onComplete();
        //         }
        //     })
        //     .subscribe(item -> System.out.println("onNext:" + item), ex -> System.out.println("异常情况" + ex), () -> System.out.println("处理完毕"));
        //
        // // 手动控制订阅
        // Flux.range(1, 1000)
        //     .subscribe(data -> System.out.println("onNext:" + data), ex -> System.err.println("异常信息: " + ex), () -> System.out.println("OnComplete"), subscription -> {
        //         subscription.request(5);
        //         subscription.cancel();
        //     });
        //

        // 实现自定义订阅者
        Subscriber<String> onComplete = new Subscriber<>() {
            volatile Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription = s;
                System.out.println("initial request for 1 element");
                this.subscription.request(1);
            }

            @Override
            public void onNext(String s) {
                System.out.println("onNext:" + s);
                System.out.println("request 1 more ");
                // 每次获取一个元素
                // 如果注释掉这个方法,表示订阅者消费完当前这个数据,后续就不继续执行了
                // this.subscription.request(1);
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("出现异常" + t);
            }

            @Override
            public void onComplete() {
                System.out.println("onComplete");
            }
        };

        Flux<String> just = Flux.just("a", "b", "c", "d");
        just.subscribe(onComplete);
    }
}
