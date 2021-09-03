package com.xyrfs.framework.spring.aspect.v1.datasource;

import com.xyrfs.common.annotations.DataSourceAnnotion;
import com.xyrfs.common.config.datasource.DynamicDataSourceContextHolder;
import com.xyrfs.common.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * 多数据源处理
 * 
 * @author
 */
@Aspect
@Order(1)
@Component
@Slf4j
public class DataSourceAspect {

    @Pointcut("@annotation(com.xyrfs.common.annotations.DataSourceAnnotion)")
    public void dsPointCut() {

    }

    @Around("dsPointCut()")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        MethodSignature signature = (MethodSignature)point.getSignature();

        Method method = signature.getMethod();

        DataSourceAnnotion dataSource = method.getAnnotation(DataSourceAnnotion.class);

        if (StringUtil.isNotNull(dataSource)) {
            DynamicDataSourceContextHolder.setDataSourceType(dataSource.value().name());
        }

        try {
            return point.proceed();
        } finally {
            // 销毁数据源 在执行方法之后
            DynamicDataSourceContextHolder.clearDataSourceType();
        }
    }
}
