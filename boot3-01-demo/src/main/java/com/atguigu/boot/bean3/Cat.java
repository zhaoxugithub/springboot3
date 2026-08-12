package com.atguigu.boot.bean3;

public class Cat implements IShout{
    @Override
    public void shout() {
        System.out.println("喵喵喵");
    }
}
