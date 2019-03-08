package com.adidas.chriniko.routesservice.init;

import com.adidas.chriniko.routesservice.error.ProcessingException;
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
import java.util.List;
import java.util.Map;

@Log4j2

@Component
public class DbInit {

    private final HikariDataSource dataSource;
    private final CitiesCsvProcessor citiesCsvProcessor;
    private final RouteGenerator routeGenerator;

    @Value("${generate.routes}")
    private boolean generateRoutes;

    @Autowired
    public DbInit(HikariDataSource dataSource, CitiesCsvProcessor citiesCsvProcessor, RouteGenerator routeGenerator) {
        this.dataSource = dataSource;
        this.citiesCsvProcessor = citiesCsvProcessor;
        this.routeGenerator = routeGenerator;
    }

    @EventListener
    public void createSchema(ContextRefreshedEvent event) {
        log.debug("will create schema now...");

        try (Connection connection = dataSource.getConnection()) {

            // Note: setup table.
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/setup.sql"));

            if (generateRoutes) {
                Map<String, List<String>> citiesByCountry = citiesCsvProcessor.getCitiesByCountry();
                routeGenerator.generate(citiesByCountry);
            }

        } catch (SQLException e) {
            log.error("error occurred during initialization of schema", e);
            throw new ProcessingException(e);
        }
    }

}
