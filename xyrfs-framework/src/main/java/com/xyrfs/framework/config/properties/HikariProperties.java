package com.xyrfs.framework.config.properties;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.Serializable;

/**
 * druid 配置属性
 *
 * @author
 */
@Configuration
public class HikariProperties implements Serializable {

    private static final long serialVersionUID = -8374942539718940542L;

    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minIdle;
    @Value("${spring.datasource.hikari.idle-timeout}")
    private long idleTimeoutMs;
    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;
    @Value("${spring.datasource.hikari.auto-commit}")
    private boolean isAutoCommit;
    @Value("${spring.datasource.hikari.pool-name}")
    private String poolName;
    @Value("${spring.datasource.hikari.max-lifetime}")
    private long maxLifetimeMs;
    @Value("${spring.datasource.hikari.connection-timeout}")
    private long connectionTimeoutMs;
    @Value("${spring.datasource.hikari.connection-test-query}")
    private String connectionTestQuery;

    public HikariDataSource dataSource(HikariDataSource datasource) {
        /**  最小空闲连接数量 */
        datasource.setMinimumIdle(minIdle);
        /**  空闲连接存活最大时间，默认600000（10分钟）*/
        datasource.setIdleTimeout(idleTimeoutMs);
        /**  连接池最大连接数，默认是10 */
        datasource.setMaximumPoolSize(maxPoolSize);
        /**  此属性控制从池返回的连接的默认自动提交行为,默认值：true */
        datasource.setAutoCommit(isAutoCommit);
        /**  连接池名 */
        datasource.setPoolName(poolName);
        /**  此属性控制池中连接的最长生命周期，值0表示无限生命周期，默认1800000即30分钟 */
        datasource.setMaxLifetime(maxLifetimeMs);
        /**  数据库连接超时时间,默认30秒，即30000 */
        datasource.setConnectionTimeout(connectionTimeoutMs);
        /**  数据库测试 */
        datasource.setConnectionTestQuery(connectionTestQuery);

        return datasource;
    }
}