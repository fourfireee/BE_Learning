package com.example.effect;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// @RestControllerAdvice：集中处理所有 Controller 抛出的异常，
// 统一转成约定的错误结构，省得每个接口各写一遍 try-catch（呼应 API 设计篇）。
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)  // 404
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponse("NOT_FOUND", e.getMessage());
    }
}
