package com.reactivepgdrivertest;

import org.testcontainers.containers.PostgreSQLContainer;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;

public class AbstractTest {

    static final PostgreSQLContainer POSTGRE_SQL_CONTAINER;
    public static final PgPool pgClient;

    static {
        POSTGRE_SQL_CONTAINER = new PostgreSQLContainer();
        POSTGRE_SQL_CONTAINER.withInitScript("schema.sql");
        POSTGRE_SQL_CONTAINER.start();

        pgClient = pgPool();
    }

    private static PgPool pgPool() {
        PgPoolOptions pgPoolOptions = new PgPoolOptions()
                .setPort(POSTGRE_SQL_CONTAINER.getFirstMappedPort())
                .setHost(POSTGRE_SQL_CONTAINER.getContainerIpAddress())
                .setDatabase(POSTGRE_SQL_CONTAINER.getDatabaseName())
                .setUser(POSTGRE_SQL_CONTAINER.getUsername())
                .setPassword(POSTGRE_SQL_CONTAINER.getPassword())
                .setMaxSize(5);

        return PgClient.pool(pgPoolOptions);
    }

}
