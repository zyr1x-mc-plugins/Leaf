package ru.lewis.point.service;

import org.dreeam.leaf.config.modules.data.DataRedis;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import ru.lewis.point.api.RedisService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public class RedisServiceImpl implements RedisService {

    private final AtomicReference<RedissonClient> client = new AtomicReference<>();

    @Override
    public RedissonClient getClient() {
        RedissonClient current = client.get();

        if (current == null) {
            throw new IllegalStateException("Redis client is not initialized yet!");
        }

        return current;
    }

    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> {
            RedissonClient created = createClient();
            client.set(created);
        });
    }

    private RedissonClient createClient() {
        Config config = new Config();
        config.setUseThreadClassLoader(false);

        SingleServerConfig singleServerConfig = config.useSingleServer()
            .setAddress("redis://" + DataRedis.dataDatabaseAddress)
            .setDatabase(0)
            .setConnectionMinimumIdleSize(5)
            .setConnectionPoolSize(10)
            .setTimeout(3000)
            .setDnsMonitoringInterval(-1);

        if (DataRedis.dataDatabasePassword != null
            && !DataRedis.dataDatabasePassword.isEmpty()) {
            singleServerConfig.setPassword(DataRedis.dataDatabasePassword);
        }

        if (DataRedis.dataDatabaseUser != null
            && !DataRedis.dataDatabaseUser.isEmpty()) {
            singleServerConfig.setUsername(DataRedis.dataDatabaseUser);
        }

        return Redisson.create(config);
    }

    public void shutdown() {
        RedissonClient current = client.getAndSet(null);

        if (current != null) {
            current.shutdown();
        }
    }
}
