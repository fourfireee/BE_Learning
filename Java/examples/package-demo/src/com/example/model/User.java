package com.example.model;

// 普通 class 示例，重点演示 package/import，而不是依赖更新的 record 语法。
public class User {
    private final String name;
    private final int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String label() {
        return name + " (" + age + ")";
    }
}
