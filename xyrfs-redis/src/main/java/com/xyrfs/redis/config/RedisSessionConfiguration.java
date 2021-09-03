package com.xyrfs.redis.config;

import com.xyrfs.redis.listener.FsHttpSessionAttributeListener;
import com.xyrfs.redis.listener.FsHttpSessionListener;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.autoconfigure.session.RedisSessionProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionListener;
import java.time.Duration;

@Configuration
@EnableConfigurationProperties(RedisSessionProperties.class)
public class RedisSessionConfiguration {

    @Value("${spring.redis.lettuce.pool.max-active}")
    private Integer maxActive;
    @Value("${spring.redis.lettuce.pool.max-idle}")
    private Integer maxIdle;
    @Value("${spring.redis.lettuce.pool.max-wait}")
    private Long maxWait;
    @Value("${spring.redis.lettuce.pool.min-idle}")
    private Integer minIdle;
    @Value("${spring.redis.commandtimeout}")
    private Long commandTimeOut;

    @Bean
    public GenericObjectPoolConfig localPoolConfig() {
        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(maxActive);
        config.setMaxIdle(maxIdle);
        config.setMaxWaitMillis(maxWait);
        config.setMinIdle(minIdle);
        return config;
    }

    @Bean
    public RedisStandaloneConfiguration redisStandaloneConfiguration(RedisProperties redisProperties) {

        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setPassword(RedisPassword.of(redisProperties.getPassword()));
        return redisStandaloneConfiguration;
    }

    @Bean("fs_lettuce_connection_factory")
    public LettuceConnectionFactory connectionFactory(
            RedisStandaloneConfiguration defaultRedisConfig,
            GenericObjectPoolConfig defaultPoolConfig
    ) {
        LettuceClientConfiguration clientConfig =
                LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(
                    commandTimeOut))
                        .poolConfig(defaultPoolConfig).build();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(defaultRedisConfig, clientConfig);
        return connectionFactory;
    }

    /**
     * 注册自定义的监听器 HttpSessionAttributeListener
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean<HttpSessionAttributeListener> getFsHttpSessionAttributeListener(){
        FsHttpSessionAttributeListener listener = new FsHttpSessionAttributeListener();
        return new ServletListenerRegistrationBean<HttpSessionAttributeListener>(listener);
    }

    /**
     * 注册自定义的监听器
     * @return
     */
    @Bean
    public ServletListenerRegistrationBean<HttpSessionListener> getFsHttpSessionListener(){
        FsHttpSessionListener listener = new FsHttpSessionListener();
        return new ServletListenerRegistrationBean<HttpSessionListener>(listener);
    }
}

