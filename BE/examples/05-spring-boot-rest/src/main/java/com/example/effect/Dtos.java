package com.example.effect;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO 用 record 最省事：不可变、自动有构造/访问器/equals。
// 接口层的输入输出结构，和数据库实体分开（这里示例简化成共用）。

// 创建请求：字段上的注解配合 @Valid 自动校验，不合法返回 400/统一错误体。
record CreateEffectRequest(
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 50) String category
) {}

// 响应结构
record EffectResponse(long id, String name, String category) {}

// 给客户端的统一错误结构（呼应 API 设计篇）
record ErrorResponse(String code, String message) {}
