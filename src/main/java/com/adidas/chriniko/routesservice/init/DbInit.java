package com.adidas.chriniko.routesservice.init;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Log4j2

@Component
public class DbInit {

    private final HikariDataSource dataSource;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Autowired
    public DbInit(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventListener
    public void createSchema(ContextRefreshedEvent event) {
       log.debug("will create schema now...");

        try (Connection connection = dataSource.getConnection(username, password)) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/setup.sql"));
        } catch (SQLException e) {
            log.error("error occurred during initialization of schema", e);
        }
    }

}
