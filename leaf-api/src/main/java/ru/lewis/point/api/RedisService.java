package ru.lewis.point.api;

import org.redisson.api.RedissonClient;

import java.util.concurrent.CompletableFuture;

public interface RedisService {
    RedissonClient getClient();
    CompletableFuture<Void> init();
}
