package com.example.effect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// @SpringBootApplication：应用入口，开启自动配置和组件扫描。
// 启动时容器会扫描本包下标了注解的类（Controller/Service 等），创建并注入它们。
@SpringBootApplication
public class EffectApplication {
    public static void main(String[] args) {
        SpringApplication.run(EffectApplication.class, args);
    }
}
