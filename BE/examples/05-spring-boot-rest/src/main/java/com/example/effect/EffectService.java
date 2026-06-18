package com.example.effect;

import org.springframework.stereotype.Service;

import java.util.List;

// @Service：业务层 Bean，放业务逻辑，尽量不依赖 HTTP（便于复用和测试）。
@Service
public class EffectService {

    private final EffectRepository repository;

    public EffectService(EffectRepository repository) {
        this.repository = repository;
    }

    public EffectResponse getById(long id) {
        // 找不到就抛业务异常，由 GlobalExceptionHandler 统一转成 404
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("effect " + id + " not found"));
    }

    public List<EffectResponse> list() {
        return repository.findAll();
    }

    public EffectResponse create(CreateEffectRequest req) {
        return repository.save(req.name(), req.category());
    }
}
