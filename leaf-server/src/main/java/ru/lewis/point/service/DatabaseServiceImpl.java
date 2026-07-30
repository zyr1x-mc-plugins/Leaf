package ru.lewis.point.service;

import org.dreeam.leaf.config.modules.data.DataDatabase;
import org.hibernate.SessionFactory;
import org.mariadb.jdbc.Driver;
import ru.lewis.point.SessionFactoryBuilder;
import ru.lewis.point.api.DatabaseService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DatabaseServiceImpl implements DatabaseService {
    private final List<Class<?>> entities = new ArrayList<>();

    private SessionFactory sessionFactory;

    @Override
    public CompletableFuture<Void> init() {
        return CompletableFuture.runAsync(() -> sessionFactory = connect());
    }

    @Override
    public DatabaseServiceImpl registerEntity(Class<?> entity) {
        entities.add(entity);
        return this;
    }

    @Override
    public DatabaseServiceImpl registerEntities(Class<?>... entities) {
        this.entities.addAll(Arrays.asList(entities));
        return this;
    }

    @Override
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    private SessionFactory connect() {
        return SessionFactoryBuilder.build(builder -> {
            builder.setClassLoader(this.getClass().getClassLoader());
            builder.setUser(DataDatabase.dataDatabaseUser);
            builder.setPassword(DataDatabase.dataDatabasePassword);
            builder.setDriver(Driver.class);
            builder.setUrl("jdbc:mariadb://" + DataDatabase.dataDatabaseAddress + "/" + DataDatabase.dataDatabaseDatabase + parametersToString(DataDatabase.dataDatabaseParameters));

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
        StringBuilder sb = new StringBuilder("?");
        for (int i = 0; i < parameters.size(); i++) {
            if (i > 0) {
                sb.append("&");
            }
            sb.append(parameters.get(i));
        }
        return sb.toString();
    }
}
