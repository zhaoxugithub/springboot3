package com.serendipity.reactor;

import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;

/**
 * ClassName ReactorDemo07
 * Description TODO
 * Author 11931
 * Date 2023-08-30:0:24
 * Version 1.0
 **/
public class ReactorDemo07 {

    @Test
    public void test01() {
        Flux.range(1, 100)
            // 10个一组
            .buffer(10)
            .subscribe(System.out::println);
    }

    @Test
    public void test02() {
        Flux.range(101, 20)
            // 当达到一定条件开始分组
            .windowUntil(item -> item % 10 == 0)
            .subscribe(window -> window.collectList()
                                       .subscribe(System.out::println));
    }

    @Test
    public void test03() {
        Flux.range(1, 7)
            .groupBy(item -> item % 2 == 0 ? "j" : "o")
            .subscribe(fg -> fg.scan(new ArrayList<>(), (list, element) -> {
                                   list.add(element);
                                   if (list.size() > 2) {
                                       list.remove(0);
                                   }
                                   return list;
                               })
                               .filter(list -> !list.isEmpty())
                               .subscribe(item -> System.out.println(fg.key() + "--" + item)));

    }
}
