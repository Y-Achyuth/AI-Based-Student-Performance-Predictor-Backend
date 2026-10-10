package com.spp.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgresDataSourceConfigurationTest {

    @Test
    void convertsRenderDatabaseUrlAndDecodesCredentials() {
        var settings = PostgresDataSourceConfiguration.resolve(
                "postgresql://render_user:p%40ss%3Aword@db.example.com:5433/student_performance",
                "jdbc:postgresql://localhost:5432/student_performance",
                "postgres",
                "postgres");

        assertEquals("jdbc:postgresql://db.example.com:5433/student_performance?sslmode=require",
                settings.jdbcUrl());
        assertEquals("render_user", settings.username());
        assertEquals("p@ss:word", settings.password());
    }

    @Test
    void preservesExplicitSslMode() {
        var settings = PostgresDataSourceConfiguration.resolve(
                "postgres://user:secret@db.example.com/student_performance?sslmode=verify-full",
                "jdbc:postgresql://localhost:5432/student_performance",
                "postgres",
                "postgres");

        assertEquals("jdbc:postgresql://db.example.com:5432/student_performance?sslmode=verify-full",
                settings.jdbcUrl());
    }

    @Test
    void rejectsNonPostgresDatabaseUrls() {
        assertThrows(IllegalArgumentException.class, () -> PostgresDataSourceConfiguration.resolve(
                "mysql://user:secret@db.example.com/student_performance",
                "jdbc:postgresql://localhost:5432/student_performance",
                "postgres",
                "postgres"));
    }
}
