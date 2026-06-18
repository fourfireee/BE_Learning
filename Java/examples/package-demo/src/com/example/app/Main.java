package com.example.app;

import com.example.model.User;

// 演示 package/import/classpath。
// 运行：在 examples/package-demo 目录执行
// javac -d out src/com/example/model/User.java src/com/example/app/Main.java
// java -cp out com.example.app.Main
public class Main {
    public static void main(String[] args) {
        User user = new User("Alice", 18);
        System.out.println(user.label());
        System.out.println("完整类名: " + user.getClass().getName());
    }
}
