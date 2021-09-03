package com.xyrfs.common.annotations;

import com.xyrfs.common.enums.DataSourceTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * 自定义多数据源切换注解
 * 
 * @author
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSourceAnnotion {
    /**
     * 切换数据源名称
     */
    DataSourceTypeEnum value() default DataSourceTypeEnum.db1;
}
