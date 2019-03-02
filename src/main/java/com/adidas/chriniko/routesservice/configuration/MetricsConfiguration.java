package com.adidas.chriniko.routesservice.configuration;

import com.adidas.chriniko.routesservice.service.CacheService;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jmx.JmxReporter;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MetricsConfiguration {

    @Bean
    public MetricRegistry metricRegistry(HikariDataSource hikariDataSource) {
        MetricRegistry metricRegistry = new MetricRegistry();

        hikariDataSource.setMetricRegistry(metricRegistry);

        return metricRegistry;
    }

    @Bean
    public Meter cacheHitFindByCityInfo(MetricRegistry metricRegistry) {
        return metricRegistry.meter(MetricRegistry.name(CacheService.class, "cache-hit-find-by-city-info"));
    }

    @Bean
    public Meter cacheMissFindByCityInfo(MetricRegistry metricRegistry) {
        return metricRegistry.meter(MetricRegistry.name(CacheService.class, "cache-miss-find-by-city-info"));
    }

    @Bean(destroyMethod = "stop")
    public JmxReporter graphiteReporter(MetricRegistry metricRegistry) {

        // add some JVM metrics (wrap in MetricSet to add better key prefixes)
        MetricSet jvmMetrics = () -> {
            Map<String, Metric> metrics = new HashMap<>();
            metrics.put("gc", new GarbageCollectorMetricSet());
            metrics.put("file-descriptors", new FileDescriptorRatioGauge());
            metrics.put("memory-usage", new MemoryUsageGaugeSet());
            metrics.put("threads", new ThreadStatesGaugeSet());
            return metrics;
        };

        metricRegistry.registerAll(jvmMetrics);

        final JmxReporter reporter = JmxReporter.forRegistry(metricRegistry).build();
        reporter.start();

        return reporter;
    }

}
