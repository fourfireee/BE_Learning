package com.example.effect;

// 业务异常：表示资源不存在。由 GlobalExceptionHandler 统一转成 404。
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
}
