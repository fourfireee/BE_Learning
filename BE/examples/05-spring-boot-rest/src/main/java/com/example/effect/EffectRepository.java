package com.example.effect;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// @Repository：数据访问层 Bean。示例用内存 Map 假装数据库，
// 真实项目这里会是 JPA/MyBatis 访问真正的数据库（见数据库篇）。
@Repository
public class EffectRepository {

    private final ConcurrentHashMap<Long, EffectResponse> data = new ConcurrentHashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public Optional<EffectResponse> findById(long id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<EffectResponse> findAll() {
        return new ArrayList<>(data.values());
    }

    public EffectResponse save(String name, String category) {
        long id = seq.incrementAndGet();
        EffectResponse e = new EffectResponse(id, name, category);
        data.put(id, e);
        return e;
    }
}
