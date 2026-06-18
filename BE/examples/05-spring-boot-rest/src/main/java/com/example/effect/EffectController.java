package com.example.effect;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// @RestController：处理 HTTP 请求，返回值自动序列化成 JSON。
// @RequestMapping：本类下所有接口的公共前缀。
@RestController
@RequestMapping("/v1/effects")
public class EffectController {

    private final EffectService service;

    // 构造函数注入：容器启动时把 EffectService 这个 Bean 传进来，不用自己 new。
    public EffectController(EffectService service) {
        this.service = service;
    }

    // GET /v1/effects/{id}
    @GetMapping("/{id}")
    public EffectResponse get(@PathVariable long id) {
        return service.getById(id);
    }

    // GET /v1/effects
    @GetMapping
    public List<EffectResponse> list() {
        return service.list();
    }

    // POST /v1/effects，请求体 JSON 自动反序列化成 CreateEffectRequest 并校验
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)  // 成功返回 201
    public EffectResponse create(@RequestBody @Valid CreateEffectRequest req) {
        return service.create(req);
    }
}
