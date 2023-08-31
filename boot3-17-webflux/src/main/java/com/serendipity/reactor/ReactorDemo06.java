package com.serendipity.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Random;

/**
 * ClassName ReactorDemo06
 * Description TODO
 * Author 11931
 * Date 2023-08-30:0:06
 * Version 1.0
 **/
public class ReactorDemo06 {

    @Test
    public void test01() throws InterruptedException {
        // sample
        Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                // 每隔100s采样一次数据
                .sample(Duration.ofMillis(100))
                .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }

    @Test
    public void test02() throws InterruptedException {
        // sampleTimeout
        Random random = new Random();
        Flux.range(1, 100)
                .delayElements(Duration.ofMillis(10))
                // 每隔100s采样一次数据
                .sampleTimeout(item -> Mono.delay(Duration.ofMillis(random.nextInt(100) + 50)), 20)
                .subscribe(System.out::println);
        Thread.sleep(10 * 1000);
    }
}
