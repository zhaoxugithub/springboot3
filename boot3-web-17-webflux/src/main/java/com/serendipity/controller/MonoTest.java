package com.serendipity.controller;

import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ClassName MonoText
 * Description TODO
 * Author 11931
 * Date 2022-12-04:0:46
 * Version 1.0
 **/
@SuppressWarnings("all")
public class MonoTest {

    // 创建Mono （发布者）
    public static void monoCreate() {

        List<Integer> list = Arrays.asList(1, 2, 3, 4, 5);
        Mono<List<Integer>> just = Mono.just(list);
        Mono<Object> empty = Mono.empty();
        Mono<Object> error = Mono.error(new Exception());
        Mono<Object> never = Mono.never();
        Mono<ArrayList> arrayListMono = Mono.fromCallable(ArrayList::new);
        Mono<Object> defer = Mono.defer(() -> {
            return Mono.empty();
        });
    }
    public static void main(String[] args) {
    }
}
