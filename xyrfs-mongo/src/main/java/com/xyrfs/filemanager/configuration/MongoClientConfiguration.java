package com.xyrfs.filemanager.configuration;

import com.mongodb.MongoClientSettings;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Copyright (C), 2018, Banyan Network Foundation
 * MongoClientConfiguration
 * Mongo Client Configuration
 *
 * @author Kevin Huang
 * @since version
 * 2018年03月21日 15:53:00
 */
@Configuration
@ConfigurationProperties(prefix = "spring.data.mongodb.client")
@AutoConfigureBefore({ MongoAutoConfiguration.class })
public class MongoClientConfiguration {
    private int minConnectionsPerHost = 5;
    private int maxConnectionsPerHost = 100;
    private int threadsAllowedToBlockForConnectionMultiplier = 5;
    private int serverSelectionTimeout = 1000 * 30;
    private int maxWaitTime = 1000 * 60 * 2;
    private int maxConnectionIdleTime;
    private int maxConnectionLifeTime;
    private int connectTimeout = 1000 * 10;
    private int socketTimeout = 0;
    private boolean socketKeepAlive = false;
    private boolean sslEnabled = false;
    private boolean sslInvalidHostNameAllowed = false;
    private boolean alwaysUseMBeans = false;

    private int heartbeatFrequency = 10000;
    private int minHeartbeatFrequency = 500;
    private int heartbeatConnectTimeout = 20000;
    private int heartbeatSocketTimeout = 20000;
    private int localThreshold = 15;

    private String requiredReplicaSetName;

    @Bean
    public MongoClientSettings getMongoClientOptions() {
        MongoClientSettings.Builder mongoClientSettings = MongoClientSettings.builder();
        mongoClientSettings.applyToConnectionPoolSettings( builder -> builder
            .maxWaitTime(maxWaitTime, TimeUnit.SECONDS)
            .maxConnectionIdleTime(maxConnectionIdleTime, TimeUnit.SECONDS)
            .maxConnectionLifeTime(maxConnectionLifeTime, TimeUnit.SECONDS)
        ).applyToSocketSettings(builder -> builder
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(socketTimeout, TimeUnit.SECONDS)
        ).applyToSslSettings(builder -> builder
            .enabled(sslEnabled)
            .invalidHostNameAllowed(sslInvalidHostNameAllowed)
        ).applyToServerSettings(builder -> builder
            .heartbeatFrequency(heartbeatFrequency, TimeUnit.SECONDS)
            .minHeartbeatFrequency(minHeartbeatFrequency, TimeUnit.SECONDS)
        ).applyToClusterSettings(builder -> builder
            // https://github.com/catkeeper1/backend_template/blob/a0e44413856360deca1a1ea487fe32a6912c3cfe/msdemo-service/src/main/java/org/ckr/msdemo/adminservice/config/MongoDbConfig.java
            .requiredReplicaSetName(requiredReplicaSetName)
            .localThreshold(localThreshold, TimeUnit.MILLISECONDS)
        );

//        ;
//        builder.minConnectionsPerHost(minConnectionsPerHost)
//            .connectionsPerHost(maxConnectionsPerHost)
//            .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier)
//            .serverSelectionTimeout(serverSelectionTimeout)
//            //                .socketKeepAlive(socketKeepAlive)
//            .alwaysUseMBeans(alwaysUseMBeans)
//
//            .heartbeatConnectTimeout(heartbeatConnectTimeout)
//            .heartbeatSocketTimeout(heartbeatSocketTimeout)

        return mongoClientSettings.build();
    }

//    @Bean
//    public MongoClientSettings getMongoClientOptions() {
//        MongoClientSettings.Builder builder = MongoClientSettings.builder();
//        builder.minConnectionsPerHost(minConnectionsPerHost)
//                .connectionsPerHost(maxConnectionsPerHost)
//                .threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier)
//                .serverSelectionTimeout(serverSelectionTimeout)
//                .maxWaitTime(maxWaitTime)
//                .maxConnectionIdleTime(maxConnectionIdleTime)
//                .maxConnectionLifeTime(maxConnectionLifeTime)
//                .connectTimeout(connectTimeout)
//                .socketTimeout(socketTimeout)
////                .socketKeepAlive(socketKeepAlive)
//                .sslEnabled(sslEnabled)
//                .sslInvalidHostNameAllowed(sslInvalidHostNameAllowed)
//                .alwaysUseMBeans(alwaysUseMBeans)
//
//                .heartbeatFrequency(heartbeatFrequency)
//                .minHeartbeatFrequency(minHeartbeatFrequency)
//                .heartbeatConnectTimeout(heartbeatConnectTimeout)
//                .heartbeatSocketTimeout(heartbeatSocketTimeout)
//                .localThreshold(localThreshold)
//
//                .requiredReplicaSetName(requiredReplicaSetName);
//
//        return builder.build();
//    }

    public int getMinConnectionsPerHost() {
        return minConnectionsPerHost;
    }

    public void setMinConnectionsPerHost(int minConnectionsPerHost) {
        this.minConnectionsPerHost = minConnectionsPerHost;
    }

    public int getMaxConnectionsPerHost() {
        return maxConnectionsPerHost;
    }

    public void setMaxConnectionsPerHost(int maxConnectionsPerHost) {
        this.maxConnectionsPerHost = maxConnectionsPerHost;
    }

    public int getThreadsAllowedToBlockForConnectionMultiplier() {
        return threadsAllowedToBlockForConnectionMultiplier;
    }

    public void setThreadsAllowedToBlockForConnectionMultiplier(int threadsAllowedToBlockForConnectionMultiplier) {
        this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
    }

    public int getServerSelectionTimeout() {
        return serverSelectionTimeout;
    }

    public void setServerSelectionTimeout(int serverSelectionTimeout) {
        this.serverSelectionTimeout = serverSelectionTimeout;
    }

    public int getMaxWaitTime() {
        return maxWaitTime;
    }

    public void setMaxWaitTime(int maxWaitTime) {
        this.maxWaitTime = maxWaitTime;
    }

    public int getMaxConnectionIdleTime() {
        return maxConnectionIdleTime;
    }

    public void setMaxConnectionIdleTime(int maxConnectionIdleTime) {
        this.maxConnectionIdleTime = maxConnectionIdleTime;
    }

    public int getMaxConnectionLifeTime() {
        return maxConnectionLifeTime;
    }

    public void setMaxConnectionLifeTime(int maxConnectionLifeTime) {
        this.maxConnectionLifeTime = maxConnectionLifeTime;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(int socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public boolean isSocketKeepAlive() {
        return socketKeepAlive;
    }

    public void setSocketKeepAlive(boolean socketKeepAlive) {
        this.socketKeepAlive = socketKeepAlive;
    }

    public boolean isSslEnabled() {
        return sslEnabled;
    }

    public void setSslEnabled(boolean sslEnabled) {
        this.sslEnabled = sslEnabled;
    }

    public boolean isSslInvalidHostNameAllowed() {
        return sslInvalidHostNameAllowed;
    }

    public void setSslInvalidHostNameAllowed(boolean sslInvalidHostNameAllowed) {
        this.sslInvalidHostNameAllowed = sslInvalidHostNameAllowed;
    }

    public boolean isAlwaysUseMBeans() {
        return alwaysUseMBeans;
    }

    public void setAlwaysUseMBeans(boolean alwaysUseMBeans) {
        this.alwaysUseMBeans = alwaysUseMBeans;
    }

    public int getHeartbeatFrequency() {
        return heartbeatFrequency;
    }

    public void setHeartbeatFrequency(int heartbeatFrequency) {
        this.heartbeatFrequency = heartbeatFrequency;
    }

    public int getMinHeartbeatFrequency() {
        return minHeartbeatFrequency;
    }

    public void setMinHeartbeatFrequency(int minHeartbeatFrequency) {
        this.minHeartbeatFrequency = minHeartbeatFrequency;
    }

    public int getHeartbeatConnectTimeout() {
        return heartbeatConnectTimeout;
    }

    public void setHeartbeatConnectTimeout(int heartbeatConnectTimeout) {
        this.heartbeatConnectTimeout = heartbeatConnectTimeout;
    }

    public int getHeartbeatSocketTimeout() {
        return heartbeatSocketTimeout;
    }

    public void setHeartbeatSocketTimeout(int heartbeatSocketTimeout) {
        this.heartbeatSocketTimeout = heartbeatSocketTimeout;
    }

    public int getLocalThreshold() {
        return localThreshold;
    }

    public void setLocalThreshold(int localThreshold) {
        this.localThreshold = localThreshold;
    }

    public String getRequiredReplicaSetName() {
        return requiredReplicaSetName;
    }

    public void setRequiredReplicaSetName(String requiredReplicaSetName) {
        this.requiredReplicaSetName = requiredReplicaSetName;
    }
}
