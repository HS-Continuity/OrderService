package com.yeonieum.orderservice.global.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

@Component
public class DatabaseConfig {

    @Autowired
    private DataSource dataSource;

    @PostConstruct
    public void initialize() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("SET SESSION group_concat_max_len = 1000000;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}