package com.atguigu.boot.bean3;

import java.util.ServiceLoader;

public class Main {

    public static void main(String[] args) {
        ServiceLoader<IShout> shouts = ServiceLoader.load(IShout.class);
        for (IShout s : shouts) {
            s.shout();
        }

        // 获取IShout 接口的实现类 有其他方法吗
        IShout shout = shouts.iterator().next();
    }
}
