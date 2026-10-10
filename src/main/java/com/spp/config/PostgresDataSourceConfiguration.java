package com.spp.config;

import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.util.Locale;

@Configuration
public class PostgresDataSourceConfiguration {

    @Bean
    public DataSource dataSource(Environment environment) {
        ConnectionSettings settings = resolve(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("SPRING_DATASOURCE_URL",
                        "jdbc:postgresql://localhost:5432/student_performance"),
                environment.getProperty("SPRING_DATASOURCE_USERNAME",
                        environment.getProperty("DB_USERNAME", "postgres")),
                environment.getProperty("SPRING_DATASOURCE_PASSWORD",
                        environment.getProperty("DB_PASSWORD", "postgres")));

        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url(settings.jdbcUrl())
                .username(settings.username())
                .password(settings.password())
                .build();
    }

    static ConnectionSettings resolve(
            String databaseUrl, String fallbackJdbcUrl, String fallbackUsername, String fallbackPassword) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return new ConnectionSettings(fallbackJdbcUrl, fallbackUsername, fallbackPassword);
        }

        if (databaseUrl.startsWith("jdbc:postgresql:")) {
            return new ConnectionSettings(databaseUrl, fallbackUsername, fallbackPassword);
        }

        URI uri;
        try {
            uri = URI.create(databaseUrl);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("DATABASE_URL must be a valid PostgreSQL connection URL", exception);
        }

        String scheme = uri.getScheme();
        if (scheme == null
                || !(scheme.equalsIgnoreCase("postgres") || scheme.equalsIgnoreCase("postgresql"))
                || uri.getHost() == null
                || uri.getPath() == null
                || uri.getPath().length() < 2) {
            throw new IllegalArgumentException("DATABASE_URL must use the postgres:// or postgresql:// format");
        }

        String query = uri.getRawQuery();
        if (query == null || !query.toLowerCase(Locale.ROOT).contains("sslmode=")) {
            query = query == null ? "sslmode=require" : query + "&sslmode=require";
        }
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + (uri.getPort() < 0 ? 5432 : uri.getPort())
                + uri.getRawPath() + "?" + query;

        String username = fallbackUsername;
        String password = fallbackPassword;
        String userInfo = uri.getUserInfo();
        if (userInfo != null) {
            int separator = userInfo.indexOf(':');
            username = separator < 0 ? userInfo : userInfo.substring(0, separator);
            password = separator < 0 ? "" : userInfo.substring(separator + 1);
        }

        return new ConnectionSettings(jdbcUrl, username, password);
    }

    record ConnectionSettings(String jdbcUrl, String username, String password) {
    }
}
