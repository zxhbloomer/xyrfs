package com.xyrfs.framework.config.datasource;

import com.xyrfs.common.enums.DataSourceTypeEnum;
import com.xyrfs.framework.config.properties.HikariProperties;
import com.xyrfs.framework.datasource.DynamicDataSource;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * hikari 配置多数据源
 *
 * @author
 */
@Configuration
public class HikariConfig {

    @Bean("db1")
    @ConfigurationProperties(prefix ="spring.datasource.hikari.db1")
    public DataSource db1DataSource(HikariProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        return properties.dataSource(dataSource);
    }

    @Bean("db2")
    @ConfigurationProperties(prefix ="spring.datasource.hikari.db2")
    @ConditionalOnProperty(prefix = "spring.datasource.hikari.db2", name = "enabled", havingValue = "true")
    public DataSource db2DataSource(HikariProperties properties) {
        HikariDataSource dataSource = DataSourceBuilder.create().type(HikariDataSource.class).build();
        return properties.dataSource(dataSource);
    }

    @Bean(name = "dynamicDataSource")
    @Primary
    public DynamicDataSource dataSource(DataSource db1, DataSource db2) {
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put(DataSourceTypeEnum.db1.name(), db1);
        targetDataSources.put(DataSourceTypeEnum.db2.name(), db2);
        return new DynamicDataSource(db1, targetDataSources);
    }
}
