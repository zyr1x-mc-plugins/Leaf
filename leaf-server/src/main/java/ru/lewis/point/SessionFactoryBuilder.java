package ru.lewis.point;

import jakarta.persistence.AttributeConverter;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.hikaricp.internal.HikariCPConnectionProvider;
import org.hibernate.hikaricp.internal.HikariConfigurationUtil;
import org.hibernate.tool.schema.Action;

import java.sql.Driver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class SessionFactoryBuilder {

    // Connect info
    private String user;
    private String password;
    private String url;
    private Class<? extends Driver> driver;

    // Cache
    private Boolean useQueryCache;
    private Class<? extends RegionFactory> cacheRegionFactory;

    // Properties
    private Map<String, String> hikariProperties = new HashMap<>();
    private Map<String, String> customProperties = new HashMap<>();

    // Misc
    private Boolean enableSqlLogging;
    private Set<Class<?>> annotatedClasses = new HashSet<>();
    private ClassLoader classLoader;
    private Class<? extends ConnectionProvider> connectionProvider = HikariCPConnectionProvider.class;
    private Action hbm2ddlAuto = Action.UPDATE;
    private Collection<AttributeConverter<?, ?>> globalAttributeConverters = new ArrayList<>();

    public SessionFactoryBuilder() {
    }

    // ---- Getters / fluent setters ----

    public String getUser() {
        return user;
    }

    public SessionFactoryBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public SessionFactoryBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public SessionFactoryBuilder setUrl(String url) {
        this.url = url;
        return this;
    }

    public Class<? extends Driver> getDriver() {
        return driver;
    }

    public SessionFactoryBuilder setDriver(Class<? extends Driver> driver) {
        this.driver = driver;
        return this;
    }

    public Boolean getUseQueryCache() {
        return useQueryCache;
    }

    public SessionFactoryBuilder setUseQueryCache(Boolean useQueryCache) {
        this.useQueryCache = useQueryCache;
        return this;
    }

    public Class<? extends RegionFactory> getCacheRegionFactory() {
        return cacheRegionFactory;
    }

    public SessionFactoryBuilder setCacheRegionFactory(Class<? extends RegionFactory> cacheRegionFactory) {
        this.cacheRegionFactory = cacheRegionFactory;
        return this;
    }

    public Map<String, String> getHikariProperties() {
        return hikariProperties;
    }

    public SessionFactoryBuilder setHikariProperties(Map<String, String> hikariProperties) {
        this.hikariProperties = hikariProperties;
        return this;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public SessionFactoryBuilder setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
        return this;
    }

    public Boolean getEnableSqlLogging() {
        return enableSqlLogging;
    }

    public SessionFactoryBuilder setEnableSqlLogging(Boolean enableSqlLogging) {
        this.enableSqlLogging = enableSqlLogging;
        return this;
    }

    public Set<Class<?>> getAnnotatedClasses() {
        return annotatedClasses;
    }

    public SessionFactoryBuilder setAnnotatedClasses(Set<Class<?>> annotatedClasses) {
        this.annotatedClasses = annotatedClasses;
        return this;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public SessionFactoryBuilder setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    public Class<? extends ConnectionProvider> getConnectionProvider() {
        return connectionProvider;
    }

    public SessionFactoryBuilder setConnectionProvider(Class<? extends ConnectionProvider> connectionProvider) {
        this.connectionProvider = connectionProvider;
        return this;
    }

    public Action getHbm2ddlAuto() {
        return hbm2ddlAuto;
    }

    public SessionFactoryBuilder setHbm2ddlAuto(Action hbm2ddlAuto) {
        this.hbm2ddlAuto = hbm2ddlAuto;
        return this;
    }

    public Collection<AttributeConverter<?, ?>> getGlobalAttributeConverters() {
        return globalAttributeConverters;
    }

    public SessionFactoryBuilder setGlobalAttributeConverters(Collection<AttributeConverter<?, ?>> globalAttributeConverters) {
        this.globalAttributeConverters = globalAttributeConverters;
        return this;
    }

    // ---- register(...) — аналог inline reified и обычного метода из Kotlin ----

    public SessionFactoryBuilder register(Class<?> entity) {
        annotatedClasses.add(entity);
        return this;
    }

    // ---- build ----

    public SessionFactory build() {
        CompletableFuture<SessionFactory> future = new CompletableFuture<>();
        Thread thread = new Thread(() -> future.complete(build0()));
        if (classLoader != null) {
            thread.setContextClassLoader(classLoader);
        }
        thread.start();
        return future.join();
    }

    private SessionFactory build0() {
        Configuration configuration = new Configuration();

        if (user != null) {
            configuration.setProperty(Environment.JAKARTA_JDBC_USER, user);
        }
        if (password != null) {
            configuration.setProperty(Environment.JAKARTA_JDBC_PASSWORD, password);
        }
        if (url != null) {
            configuration.setProperty(Environment.JAKARTA_JDBC_URL, url);
        }
        if (driver != null) {
            configuration.setProperty(Environment.JAKARTA_JDBC_DRIVER, driver.getName());
        }
        if (connectionProvider != null) {
            configuration.setProperty(Environment.CONNECTION_PROVIDER, connectionProvider.getName());
        }
        if (hbm2ddlAuto != null) {
            configuration.setProperty(Environment.HBM2DDL_AUTO, hbm2ddlAuto.getExternalHbm2ddlName());
        }

        if (useQueryCache != null) {
            configuration.setProperty(Environment.USE_QUERY_CACHE, useQueryCache.toString());
        }
        if (cacheRegionFactory != null) {
            configuration.setProperty(Environment.CACHE_REGION_FACTORY, cacheRegionFactory.getName());
        }

        if (enableSqlLogging != null) {
            configuration.setProperty(Environment.SHOW_SQL, enableSqlLogging.toString());
            configuration.setProperty(Environment.HIGHLIGHT_SQL, enableSqlLogging.toString());
        }

        for (Class<?> annotatedClass : annotatedClasses) {
            configuration.addAnnotatedClass(annotatedClass);
        }

        for (Map.Entry<String, String> entry : hikariProperties.entrySet()) {
            customProperties.put(HikariConfigurationUtil.CONFIG_PREFIX + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, String> entry : customProperties.entrySet()) {
            configuration.setProperty(entry.getKey(), entry.getValue());
        }

        for (AttributeConverter<?, ?> converter : globalAttributeConverters) {
            configuration.addAttributeConverter(converter);
        }

        return configuration.buildSessionFactory();
    }

    // ---- статический DSL-метод, аналог companion object в Kotlin ----

    public static SessionFactory build(Consumer<SessionFactoryBuilder> builderConsumer) {
        SessionFactoryBuilder builder = new SessionFactoryBuilder();
        builderConsumer.accept(builder);
        return builder.build();
    }
}
