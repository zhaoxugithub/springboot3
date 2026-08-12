package com.atguigu.boot.bean3;

public class Dog implements IShout{
    @Override
    public void shout() {
        System.out.println("汪汪汪");
    }
}
