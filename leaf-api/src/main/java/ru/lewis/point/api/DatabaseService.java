package ru.lewis.point.api;

import org.hibernate.SessionFactory;

import java.util.concurrent.CompletableFuture;

public interface DatabaseService {
    SessionFactory getSessionFactory();
    DatabaseService registerEntity(Class<?> entity);
    DatabaseService registerEntities(Class<?>... entities);
    CompletableFuture<Void> init();
}
