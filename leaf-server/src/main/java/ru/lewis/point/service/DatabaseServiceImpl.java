package ru.lewis.point.service;

import org.dreeam.leaf.config.modules.data.DataDatabase;
import org.hibernate.SessionFactory;
import org.mariadb.jdbc.Driver;
import ru.lewis.point.CompositeClassLoader;
import ru.lewis.point.SessionFactoryBuilder;
import ru.lewis.point.api.DatabaseService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DatabaseServiceImpl implements DatabaseService {

    private final List<Class<?>> entities = new ArrayList<>();
    private final Set<ClassLoader> classLoaders = new HashSet<>();

    private CompletableFuture<SessionFactory> sessionFactoryFuture;

    @Override
    public synchronized CompletableFuture<Void> init() {
        if (sessionFactoryFuture != null) {
            return sessionFactoryFuture.thenApply(ignored -> null);
        }

        // Фиксируем entities/classLoaders до запуска async.
        List<Class<?>> entitiesSnapshot = List.copyOf(entities);
        Set<ClassLoader> classLoadersSnapshot = Set.copyOf(classLoaders);

        sessionFactoryFuture = CompletableFuture.supplyAsync(
            () -> connect(entitiesSnapshot, classLoadersSnapshot)
        );

        return sessionFactoryFuture.thenApply(ignored -> null);
    }

    @Override
    public synchronized DatabaseServiceImpl registerEntity(Class<?> entity) {
        checkNotInitialized();

        entities.add(entity);
        classLoaders.add(entity.getClassLoader());

        return this;
    }

    @Override
    public synchronized DatabaseService registerEntities(Class<?>... entities) {
        checkNotInitialized();

        for (Class<?> entity : entities) {
            this.entities.add(entity);
            this.classLoaders.add(entity.getClassLoader());
        }

        return this;
    }

    @Override
    public SessionFactory getSessionFactory() {
        CompletableFuture<SessionFactory> future;

        synchronized (this) {
            future = sessionFactoryFuture;
        }

        if (future == null) {
            throw new IllegalStateException(
                "Database has not been initialized yet. Call init() first."
            );
        }

        // Ждём завершения подключения, если оно ещё идёт.
        return future.join();
    }

    private SessionFactory connect(
        List<Class<?>> entities,
        Set<ClassLoader> classLoaders
    ) {
        CompositeClassLoader classLoader = new CompositeClassLoader(
            this.getClass().getClassLoader(),
            new ArrayList<>(classLoaders)
        );

        return SessionFactoryBuilder.build(builder -> {
            builder.setClassLoader(classLoader);

            builder.setUser(DataDatabase.dataDatabaseUser);
            builder.setPassword(DataDatabase.dataDatabasePassword);
            builder.setDriver(Driver.class);

            builder.setUrl(
                "jdbc:mariadb://" +
                    DataDatabase.dataDatabaseAddress +
                    "/" +
                    DataDatabase.dataDatabaseDatabase +
                    parametersToString(DataDatabase.dataDatabaseParameters)
            );

            builder.getHikariProperties().put(
                "maximumPoolSize",
                String.valueOf(Runtime.getRuntime().availableProcessors())
            );

            builder.getHikariProperties().put(
                "connectionTimeout",
                String.valueOf(TimeUnit.SECONDS.toMillis(10))
            );

            builder.getHikariProperties().put(
                "poolName",
                "leaf/mariadb"
            );

            for (Class<?> entity : entities) {
                builder.register(entity);
            }
        });
    }

    private String parametersToString(List<String> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder("?");

        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                sb.append("&");
            }

            sb.append(parameters.get(i));
        }

        return sb.toString();
    }

    private synchronized void checkNotInitialized() {
        if (sessionFactoryFuture != null) {
            throw new IllegalStateException(
                "Cannot register entities after database initialization has started."
            );
        }
    }
}
