package com.xyrfs.core.config.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.xyrfs.core.config.mybatis.sqlinjector.FsSqlInjector;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author zxh
 */
@Component
@Slf4j
@Configuration
@MapperScan("com.xyrfs.core.mapper")
public class MybatisPlusConfig  {

    /**
     * 自定义 SqlInjector
     * 里面包含自定义的全局方法
     */
    @Bean
    public FsSqlInjector fsSqlInjector() {
        return new FsSqlInjector();
    }
}
