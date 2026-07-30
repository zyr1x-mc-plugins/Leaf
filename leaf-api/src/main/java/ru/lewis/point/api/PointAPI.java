package ru.lewis.point.api;

public class PointAPI {
    private static PointAPI INSTANCE;

    public static PointAPI get() {
        return INSTANCE;
    }

    public static void init(DatabaseService databaseService, RedisService redisService) {
        if (INSTANCE != null) return;
        INSTANCE = new PointAPI(databaseService, redisService);
    }

    private final DatabaseService databaseService;
    private final RedisService redisService;

    public PointAPI(DatabaseService databaseService, RedisService redisService) {
        this.databaseService = databaseService;
        this.redisService = redisService;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public RedisService getRedisService() {
        return redisService;
    }
}
