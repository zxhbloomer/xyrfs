package com.xyrfs.managerStarter.starter;

import com.xyrfs.common.properies.FsConfigProperies;
import com.xyrfs.security.properties.FsSecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author zxh
 */
@SpringBootApplication(
    exclude = { DataSourceAutoConfiguration.class },
    scanBasePackages = {
            "com.xyrfs.framework",
            "com.xyrfs.*",
            "com.xyrfs.common",
            "com.xyrfs.security",
            "com.xyrfs.redis",
            "com.xyrfs.manager.controller",
        })
@EnableTransactionManagement
@EntityScan(basePackages = {"com.xyrfs.*"})
@Slf4j
@EnableConfigurationProperties({FsSecurityProperties.class, FsConfigProperies.class})
@EnableCaching
@EnableRabbit
@ServletComponentScan
public class ManagerServerStart {

    public static ConfigurableApplicationContext config;

    public static void main(String[] args) {
        log.info("-----------------------启动开始-------------------------");
        SpringApplication.run(ManagerServerStart.class, args);
        log.info("-----------------------启动完毕-------------------------");
    }
}
